package com.org.scraper_bkd.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.scraper_bkd.converter.JsonNodeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductScraperDTO {


    String brand;
    String productURL;
    String productName;
    String currency;
    long price;
    long mrp;
    float ratings;
    int reviews;
    String stock_status;
    String imageURL;

    JsonNode specs;
    JsonNode technicalDetails;



}
