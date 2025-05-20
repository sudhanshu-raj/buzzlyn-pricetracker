package com.org.scraper_bkd.model;

/**
 * This stores the price data  of each product
 * this is like the price logs of the product
 */

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "price_history")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    ProductScraperModel productScraperModel;

    long price;
    LocalDateTime timestamp;
}