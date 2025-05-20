package com.org.scraper_bkd_security.services;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitService {
    private final Logger logger = LoggerFactory.getLogger(RateLimitService.class);


    @Autowired
    private ProxyManager<String> proxyManager;


    // User-specific rate-limiting,using for otp rate limiting (10 requests per hour)
    public boolean isUserRateLimited(String key) {
        Bucket userBucket = proxyManager.builder().build(key, BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofHours(1)))
                .build());

        ConsumptionProbe probe = userBucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            logger.warn("User {} is rate-limited. Remaining tokens: {}", key, probe.getRemainingTokens());
            return true;
        }

        logger.debug("User {} consumed token. Remaining tokens: {}", key, probe.getRemainingTokens());
        return false;
    }

    // IP-specific rate-limiting (10 requests per hour)
    public boolean isIpRateLimited(String ip) {
        Bucket ipBucket = proxyManager.builder().build(ip, BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofHours(1)))
                .build());

        ConsumptionProbe probe = ipBucket.tryConsumeAndReturnRemaining(1);
        logger.info("Checking IP rate limit for {}: {} remaining tokens", ip, probe.getRemainingTokens());
        if (!probe.isConsumed()) {
            logger.warn("IP {} is rate-limited. Remaining tokens: {}", ip, probe.getRemainingTokens());
        }
        return !probe.isConsumed();
    }

    /**
     * This used to prevent bruteforce attack where input is otp
     */
    public boolean isOtpBruteForceProtected(String userId, String ip,String authType) {
        // Device-specific -   5 attempts per 10 min
        String deviceKey = authType+":device::" + userId + "::" + ip;
        Bucket deviceBucket = proxyManager.builder().build(deviceKey,
                BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(5, Duration.ofMinutes(10)))
                        .build());

        // Account-wide -   10 attempts per 10 min
        String accountKey = authType+":account::" + userId;
        Bucket accountBucket = proxyManager.builder().build(accountKey,
                BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(10, Duration.ofMinutes(10)))
                        .build());

        return deviceBucket.getAvailableTokens() <= 0 || accountBucket.getAvailableTokens() <= 0;
    }
    public void resetRateLimitOnSuccess(String userId, String ip,String authType) {
        // Reset device-specific rate limit
        String deviceKey = authType+":device::" + userId + "::" + ip;
        proxyManager.removeProxy(deviceKey);

        // Optionally: Don't reset the account-wide limit
        // This maintains protection against distributed attacks
        // while rewarding successful logins from known devices

        logger.debug("Rate limit reset for device key: {}", deviceKey);
    }

    /**
      * Record a failed attempt for both device and account limits
     */
    public void recordFailedLoginAttempt(String userId, String ip,String authType) {
        // Record for device-specific bucket (5 attempts per 10 min)
        String deviceKey = authType+":device::" + userId + "::" + ip;
        Bucket deviceBucket = proxyManager.builder().build(deviceKey,
                BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(5, Duration.ofMinutes(10)))
                        .build());

        // Record for account-wide bucket (10 attempts per 10 min)
        String accountKey = authType+":account::" + userId;
        Bucket accountBucket = proxyManager.builder().build(accountKey,
                BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(10, Duration.ofMinutes(10)))
                        .build());

        // Consume tokens from both buckets
        ConsumptionProbe deviceProbe = deviceBucket.tryConsumeAndReturnRemaining(1);
        ConsumptionProbe accountProbe = accountBucket.tryConsumeAndReturnRemaining(1);

        logger.debug("Failed OTP attempt: device={} remaining, account={} remaining",
                deviceProbe.getRemainingTokens(), accountProbe.getRemainingTokens());
    }

}
