package com.org.scraper_bkd_security.dto;

import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.owasp.encoder.Encode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerDTO {

    @NotBlank(message = "firstname should not blank")
    @Size(max = 50,message="firstname exceeds 50 char")
    String firstName;

    @Size(max = 50,message="firstname exceeds 50 char")
    String lastName;

    @Size(max = 20,message="pt_ky can't exceeds more than 20 char")
    String email_pt_ky;

    @Size(max = 20,message="pt_ky can't exceeds more than 20 char")
    String phone_pt_ky;

    @NotBlank(message = "phone number should not blank")
    @Size(max = 15,message="phone number cannot exceeds 15 char")
    String phoneNumber;

    @NotBlank(message="Email should not blank")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message="Password can't be empty")
    @Size(min=8,message="Password should be minimum 8 char long")
    String password;

    byte[] profileImage;

//    public String getFirstName() {
//        return SanitizationUtil.sanitize(firstName);
//    }
//
//    public String getLastName() {
//        return SanitizationUtil.sanitize(lastName);
//    }
//
//    public String getEmail() {
//        return SanitizationUtil.sanitize(email);
//    }
//
//    public String getPhoneNumber() {
//        return SanitizationUtil.sanitize(phoneNumber);
//    }

}
