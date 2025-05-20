package com.org.scraper_bkd.repo;

import com.org.scraper_bkd.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepo extends JpaRepository<PriceHistory, Long> {


    List<PriceHistory> findByProductScraperModelId(long id);

    @Query("SELECT p from PriceHistory p " +
            "WHERE p.productScraperModel.id = :id")
    List<PriceHistory> findByProductId(@Param("id") long id);

    @Query("SELECT p FROM PriceHistory p " +
            "WHERE p.productScraperModel.id = :productId " +
            "AND p.timestamp >= :date")
    List<PriceHistory> findByProductIdAndDate(@Param("productId") long productId,
                                              @Param("date") LocalDateTime date);

}
