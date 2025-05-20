package com.org.scraper_bkd.repo;

import com.org.scraper_bkd.enums.TrackingStatus;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.ProductScraperModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductScraperRepo extends JpaRepository<ProductScraperModel, Long> {

    @Query("SELECT p FROM ProductScraperModel p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    List<ProductScraperModel> findByNameAndBrand(@Param("name") String name, @Param("brand") String brand);

    @Modifying
    @Transactional
    @Query("UPDATE ProductScraperModel p SET p.price = :new_price WHERE p.id = :id")
    void updatePrice(@Param("new_price") long new_price, @Param("id") long id);

    @Modifying
    @Transactional
    @Query("UPDATE ProductScraperModel p SET p.mrp = :new_mrp WHERE p.id = :id")
    void updateMRP(@Param("new_mrp") long new_mrp, @Param("id") long id);

    @Modifying
    @Transactional
    @Query("UPDATE ProductScraperModel p SET p.stock_status = :new_status WHERE p.id = :id")
    void updateStockStatus(@Param("new_status") String new_status, @Param("id") long id);

    @Modifying
    @Transactional
    @Query("UPDATE ProductScraperModel p SET p.ratings = :new_rating WHERE p.id = :id")
    void updateRatings(@Param("new_rating") float new_rating, @Param("id") long id);

    @Modifying
    @Transactional
    @Query("UPDATE ProductScraperModel p SET p.reviews = :new_reviews WHERE p.id = :id")
    void updateReviews(@Param("new_reviews") int new_reviews, @Param("id") long id);


    List<ProductScraperModel> findByUrlIdentifier(String urlIdentifier);


    @Query("SELECT p FROM ProductScraperModel p " +
            "WHERE p.tracking_status = false " +
            "AND p.updatedOn  < :timeThreshold")
    List<ProductScraperModel> findNonTrackingProducts(@Param("timeThreshold") LocalDateTime timeThreshold);
}
