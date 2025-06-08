package com.org.scraper_bkd_security.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.LOGINOTP_PREFIX;
import static com.org.scraper_bkd_security.constants.ApplicationConstants.OTP_EXPIRE_TIME;

@Service
@RequiredArgsConstructor
public class SignInOtpService {

    private static final Logger logger = LoggerFactory.getLogger(SignInOtpService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationService notificationService;
    private static final String OTP_PREFIX = "otp:";

    public String generateAndSendOtp(String email,String prefix) {
        try {
            String otp = String.valueOf(new Random().nextInt(9000) + 1000);
            String key = prefix + email;
            redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_EXPIRE_TIME));
            // TODO: send email logic
            if (prefix.equalsIgnoreCase(LOGINOTP_PREFIX)) {
                notificationService.sendAuthenticationOTPEmail(otp,email);
            }
            else{
                notificationService.sendPasswordRecoveryOTPEmail(otp,email);
            }
            return otp;
        }
        catch (Exception e){
            logger.error("Error at generateAndSendOtp : {}",e.getMessage());
            throw new RuntimeException("Unable to send the otp");
        }
    }

    public boolean verifyOtp(String email, String otp,String prefix) {
        try {
            String key = prefix + email;
            String storedOtp = (String) redisTemplate.opsForValue().get(key);
            if (storedOtp == null) return false;

            boolean isValid = storedOtp.equals(otp);
            if (isValid) redisTemplate.delete(key); // Optional: delete OTP after use
            return isValid;
        }
        catch (Exception e){
            logger.error("Error at verifyOtp : {}",e.getMessage());
            throw new RuntimeException("Unable to verify the otp");
        }
    }

    public void invalidateOtp(String email,String prefix) {
        redisTemplate.delete(prefix + email);
    }
}
