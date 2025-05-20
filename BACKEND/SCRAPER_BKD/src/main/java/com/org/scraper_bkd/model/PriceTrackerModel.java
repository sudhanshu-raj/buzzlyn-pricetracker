package com.org.scraper_bkd.model;

/**
 * This stores the each request user has made for the product for the price alert
 */

import com.org.scraper_bkd.enums.TrackingStatus;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
@Table(name = "price_tracker")
public class PriceTrackerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    ProductScraperModel productScraperModel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="priceTrackerUser_id")
    PriceTrackerUsers priceTrackerUsers;

    long first_time_price;
    long current_time_price;

    @Enumerated(EnumType.STRING)
    TrackingStatus status ;

    private LocalDateTime lastChecked;
    private LocalDateTime nextScrapeTime;

    @CreationTimestamp
    LocalDateTime createdOn;

}
