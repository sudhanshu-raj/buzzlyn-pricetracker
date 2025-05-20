package com.org.scraper_bkd.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class PincodeTrackerDTO {

    boolean isStockAvailable;
    String deliveryDate;

}
