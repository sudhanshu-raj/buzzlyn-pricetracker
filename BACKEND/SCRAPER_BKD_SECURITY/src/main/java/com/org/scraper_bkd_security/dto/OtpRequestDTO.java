package com.org.scraper_bkd_security.dto;

import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OtpRequestDTO {

    @NotBlank(message = "user_id can't be blank")
    @Size(max=100,message = "User id can't be more than 100 char")
    String userId;

    @NotBlank(message="isEmail can't be empty")
    @Size(max=5,message = "isEmail can't be more than 5 char")
    String isEmail;


    @Size(max = 20,message="otpId can't exceeds more than 20 char")
    String otpId;

//    public String getUserId() {
//        return SanitizationUtil.sanitize(userId);
//    }

}
