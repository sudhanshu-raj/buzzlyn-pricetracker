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
public class OAuthUserRequest {

    @NotBlank(message = "email can't not be blank")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Phone Number can't be blank")
    @Size(max = 15,message = "Phone Number can't exceeds 15 chars")
    String phoneNumber;

    @NotBlank(message = "pt_ky can't be blank")
    @Size(max=20,message = "pt_ky can't exceeds 20 chars")
    String pt_ky;

//    public String getPhoneNumber() {
//        return SanitizationUtil.sanitize(phoneNumber);
//    }
}
