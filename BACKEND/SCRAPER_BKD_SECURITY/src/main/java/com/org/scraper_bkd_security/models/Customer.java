package com.org.scraper_bkd_security.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @NotBlank(message = "firstname should not blank")
    @Size(max = 50,message="firstname exceeds 50 char")
    String firstName;
    String lastName;

    @NotBlank(message = "phone number should not blank")
    @Size(max = 15,message="phone number cannot exceeds 15 char")
    String phoneNumber;

    @NotBlank(message="Email should not blank")
    @Email(message = "Invalid email format")
    String email;

    @JsonIgnore
    String role;

    @JsonIgnore
    boolean isPhoneVerified;

    @JsonIgnore
    boolean isEmailVerified;

    @JsonIgnore
    boolean isGoogleOAuthUser;

    @NotBlank(message="Password can't be empty")
    @Size(min=6,message="Password should be minimum 8 char long")
    @JsonIgnore
    String password;

    @Lob
    @Column(name = "profile_pic", columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] imageData;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    Date created;

    @UpdateTimestamp
    @JsonIgnore
    Date updated;

}
