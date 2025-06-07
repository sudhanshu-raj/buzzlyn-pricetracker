package com.org.scraper_bkd_security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.org.scraper_bkd_security.util.SanitizationUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OtpResponseDTO {

    @NotBlank(message = "pt_ky can't be blank")
    @Size(max = 20,message="pt_ky can't exceeds more than 20 char")
    String pt_ky;  // here pt_ky is encoded version of otpId


   @JsonIgnore
    int otp;

    @NotBlank(message = "isEmail should not blank")
    @Size(max = 5,message = "isEmail should not exceeds 5 char")
    String isEmail;

}
