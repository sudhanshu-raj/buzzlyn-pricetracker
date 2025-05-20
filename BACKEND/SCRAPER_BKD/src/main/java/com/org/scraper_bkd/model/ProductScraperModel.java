package com.org.scraper_bkd.model;

/**
 * This stores the actual data of product scraped
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.org.scraper_bkd.converter.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "product_scraper")
public class ProductScraperModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String brand;

    @Column(length = 5000)
    String productURL;

    @Column(columnDefinition = "TEXT")
    String productName;
    long price;
    long mrp;
    String currency;

    float ratings;
    int reviews;
    String stock_status;

    boolean tracking_status;


    @Column(length = 4000)
    String imageURL;

    @Convert(converter = JsonNodeConverter.class) // Apply JSON Converter
    @Column(columnDefinition = "TEXT") // Store as String in DB
    JsonNode specs;

    @Convert(converter = JsonNodeConverter.class) // Apply JSON Converter
    @Column(columnDefinition = "TEXT") // Store as String in DB
    JsonNode technicalDetails;

    @JsonIgnore
    String urlIdentifier;

    @CreationTimestamp
    @JsonIgnore
    LocalDateTime createdOn;

    @UpdateTimestamp
    @JsonIgnore
    LocalDateTime updatedOn;


}
