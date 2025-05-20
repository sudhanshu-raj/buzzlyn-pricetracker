package com.org.scraper_bkd.dto;

import com.org.scraper_bkd.enums.NotificationFrequency;
import com.org.scraper_bkd.model.ProductScraperModel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceTrackerRequest {


    @NotNull(message = "Product id is required")
    @Min(value = 1, message = "Product id must be at least 1")
    Long productId;

    // this only required for existing tracker and user want to update their settings
    long userConfigId;

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
    //on daily,weekly or monthly basis .
    boolean notificationFrequencySet;
    @Enumerated(EnumType.STRING)
    NotificationFrequency notificationFrequencyValue;
    int customNotificationDays;

    //here we not need to pass this value as service class will handle this
    ProductScraperModel productScraperModel;
}
