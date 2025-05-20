package com.org.scraper_bkd_security.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpOtpManage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    boolean isEmail;

    @NotBlank(message = "user_id can't be blank")
    @Size(max=100,message = "User id can't be more than 100 char")
    String userID;

    @Min(value = 1000, message = "OTP must be a 4-digit number")
    @Max(value = 9999, message = "OTP must be a 4-digit number")
    int otp;

    int otpCount;


    boolean isExpired;
    boolean isVerified;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    Date created;

    @UpdateTimestamp
    @JsonIgnore
    Date updated;
}
