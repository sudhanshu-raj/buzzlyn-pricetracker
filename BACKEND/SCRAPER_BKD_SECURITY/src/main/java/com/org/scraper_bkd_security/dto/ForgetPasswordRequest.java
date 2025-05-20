package com.org.scraper_bkd_security.dto;

import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgetPasswordRequest {

    @Email(message = "Invalid email format")
    String email;

    @Size(max = 4,message = "OTP can't exceeds four chars")
    String otp;

    String password;

//    public String getEmail() {
//        return SanitizationUtil.sanitize(email);
//    }
}
