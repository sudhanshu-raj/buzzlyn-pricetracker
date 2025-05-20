package com.org.scraper_bkd_security.dto;

import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordLoginRequest {


    @NotBlank(message = "Email can't be empty")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Password can't be empty")
    String password;

//    public String getEmail() {
//        return SanitizationUtil.sanitize(email);
//    }
}
