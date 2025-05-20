package com.org.scraper_bkd_security.dto;

import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckUserExistsRequest {

    @Email(message = "Invalid email format")
    String email;

    @Size(max = 15, message = "Phone number can't exists 15 chars")
    String phoneNumber;

//    public String getEmail() {
//        return SanitizationUtil.sanitize(email);
//    }
//
//    public String getPhoneNumber() {
//        return SanitizationUtil.sanitize(phoneNumber);
//    }
}
