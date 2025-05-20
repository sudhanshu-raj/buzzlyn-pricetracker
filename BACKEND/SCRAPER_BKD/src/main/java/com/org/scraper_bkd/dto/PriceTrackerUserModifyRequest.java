package com.org.scraper_bkd.dto;

import com.org.scraper_bkd.enums.NotificationFrequency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceTrackerUserModifyRequest {


    long priceTrackerID;

    boolean emailSMSEnabled;
    boolean phoneSMSEnabled;
    boolean whatsappSMSEnabled;

    boolean automaticAlert;
    boolean customPriceAlert;
    boolean stockAlert;

    long customPrice;

    String pincodeStock;

    //here isNotificationFrequencySet means wether user wants the summary of product price changes  or not
    //on daily,weekly or monthly basis
    boolean notificationFrequencySet;
    @Enumerated(EnumType.STRING)
    NotificationFrequency notificationFrequencyValue;
}
