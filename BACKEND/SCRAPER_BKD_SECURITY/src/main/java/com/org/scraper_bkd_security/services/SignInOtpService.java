package com.org.scraper_bkd_security.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.OTP_EXPIRE_TIME;

@Service
@RequiredArgsConstructor
public class SignInOtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OTP_PREFIX = "otp:";

    public String generateAndSendOtp(String email,String prefix) {
        String otp = String.valueOf(new Random().nextInt(9000) + 1000);
        String key = prefix + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_EXPIRE_TIME));
        // TODO: send email logic
        System.out.println("OTP for " + email + " is " + otp);
        return otp;
    }

    public boolean verifyOtp(String email, String otp,String prefix) {
        String key = prefix + email;
        String storedOtp = (String) redisTemplate.opsForValue().get(key);
        if (storedOtp == null) return false;

        boolean isValid = storedOtp.equals(otp);
        if (isValid) redisTemplate.delete(key); // Optional: delete OTP after use
        return isValid;
    }

    public void invalidateOtp(String email,String prefix) {
        redisTemplate.delete(prefix + email);
    }
}
