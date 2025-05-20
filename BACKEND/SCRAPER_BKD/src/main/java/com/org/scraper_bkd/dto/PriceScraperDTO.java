package com.org.scraper_bkd.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceScraperDTO {

    String title;
    long price;
    long mrp;
    String stock;
    int reviews;
    float ratings;
    String brand;

    @JsonIgnore
    boolean tracking_status;
    @JsonIgnore
    LocalDateTime updated_on;
}
