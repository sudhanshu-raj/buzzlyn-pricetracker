package com.org.scraper_bkd_security.controllers;

import com.org.scraper_bkd_security.dto.ForgetPasswordRequest;
import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.repo.UserRepo;
import com.org.scraper_bkd_security.services.RateLimitService;
import com.org.scraper_bkd_security.services.SignInOtpService;
import com.org.scraper_bkd_security.util.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.FORGETPASSWORD_OTP_PREFIX_;
import static com.org.scraper_bkd_security.constants.ApplicationConstants.LOGINOTP_PREFIX;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ForgetPasswordController {

    private static final Logger logger = LoggerFactory.getLogger(SignInController.class);


    private final RateLimitService rateLimitService;
    private final SignInOtpService signInOtpService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/forgetPass-otp")
    public ResponseEntity<?> generateForgetPasswordOtp(@RequestParam(required = true) @Email(message = "Invalid email format") String email,
             HttpServletRequest request){
        try {
            String clientIp = request.getRemoteAddr();
            String key = "forgetPass::" + email + ":" + clientIp;
            if (rateLimitService.isUserRateLimited(key)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many OTP requests for this user. Please try again after an hour.");
            }
            email=Encode.forHtml(email);
            Customer existedUser=userRepo.findUserByEmail(email);
            if(existedUser==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not exists");
            }

            String otp = signInOtpService.generateAndSendOtp(email, FORGETPASSWORD_OTP_PREFIX_);
            return ResponseEntity.ok("OTP sent to email : " + otp);
        }
        catch(Exception e){
            logger.error("Unexpected error at generateForgetPasswordOtp : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error,try again");
        }
    }

    @PostMapping("/verifyForgetPass-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody ForgetPasswordRequest req) {
        try {
            if(req==null || req.getEmail()==null || req.getEmail().isEmpty() || req.getOtp()==null || req.getOtp().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required Parameters");
            }
            String email = req.getEmail();
            String otp = req.getOtp();

            if (signInOtpService.verifyOtp(email, otp, FORGETPASSWORD_OTP_PREFIX_)) {
                String token = jwtTokenUtils.generatePasswordResetToken(req.getEmail());
                redisTemplate.opsForValue().set("resetToken:" + req.getEmail(), "valid", Duration.ofMinutes(5));
                return ResponseEntity.ok(token);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }
        catch (Exception e){
            logger.error("Unexpected error at verifyOtp : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error,try again");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestHeader("Authorization") String token,
                                                @RequestBody ForgetPasswordRequest req) {
        try {
            if(req==null || req.getPassword()==null || req.getPassword().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password can't be empty");
            }
            Claims claims ;
            try {
                claims = jwtTokenUtils.extractAllClaims(token);
            } catch (ExpiredJwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired");
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            String email = claims.getSubject();
            String type = claims.get("type", String.class);

            if (!"password_reset".equals(type)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
            }

            String redisKey = "resetToken:" + email;
            String status = redisTemplate.opsForValue().get(redisKey);

            if (!"valid".equals(status)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired or already used");
            }

            // Update password
            Customer user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userRepo.save(user);

            // Invalidate reset token
            redisTemplate.delete(redisKey);

            return ResponseEntity.ok("Password successfully reset");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }
}
