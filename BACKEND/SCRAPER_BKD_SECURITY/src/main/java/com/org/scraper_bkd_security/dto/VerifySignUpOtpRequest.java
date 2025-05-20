package com.org.scraper_bkd_security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifySignUpOtpRequest {

    @NotBlank(message = "pt_ky can't be blank")
    @Size(max = 20,message="pt_ky can't exceeds more than 20 char")
    String pt_ky;  // here pt_ky is encoded version of otpId

    @Min(value = 1000, message = "OTP must be a 4-digit number")
    @Max(value = 9999, message = "OTP must be a 4-digit number")
    int otp;

    @NotBlank(message = "isEmail should not blank")
    @Size(max = 5,message = "isEmail should not exceeds 5 char")
    String isEmail;
}
