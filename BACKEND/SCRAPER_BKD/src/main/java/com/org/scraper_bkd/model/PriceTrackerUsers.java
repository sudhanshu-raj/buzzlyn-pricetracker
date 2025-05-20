package com.org.scraper_bkd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.org.scraper_bkd.enums.NotificationFrequency;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "price_tracker_users")
public class PriceTrackerUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String email;
    String phoneNumber;

    boolean emailSMSEnabled;
    boolean phoneSMSEnabled;
    boolean whatsappSMSEnabled;
    boolean pushSMSEnabled;

    boolean automaticAlert;
    boolean customPriceAlert;
    boolean stockAlert;

    long customPrice;

    boolean pincodeStockTracking;
    String pincode;

    //here isNotificationFrequencySet means wether user wants the summary of product price changes  or not
    //on daily,weekly or monthly basis
    boolean notificationFrequencySet;

    @Enumerated(EnumType.STRING)
    NotificationFrequency notificationFrequencyValue;


    int customNotificationDays;

    boolean automaticAlertSuccess;
    boolean customPriceAlertSuccess;
    boolean pincodeAlertSuccess;

    LocalDateTime notificationUpdateDate;

    @CreationTimestamp
    @JsonIgnore
    LocalDateTime createdOn;

    @UpdateTimestamp
    @JsonIgnore
    LocalDateTime updatedOn;


}
