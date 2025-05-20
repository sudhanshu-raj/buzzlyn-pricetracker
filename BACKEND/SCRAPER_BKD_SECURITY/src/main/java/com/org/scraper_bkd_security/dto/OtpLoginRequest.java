package com.org.scraper_bkd_security.dto;

import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpLoginRequest {

    @Email(message = "Email should in proper format")
    String email;

    @NotBlank(message = "OTP should not be empty")
    @Size(max = 6,message = "OTP should be six digits")
    String otp;

//    public String getEmail() {
//        return SanitizationUtil.sanitize(email);
//    }


}
