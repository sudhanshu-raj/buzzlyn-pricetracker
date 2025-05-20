package com.org.scraper_bkd.repo;

import com.org.scraper_bkd.enums.TrackingStatus;
import com.org.scraper_bkd.model.PriceTrackerModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface PriceTrackerRepo extends JpaRepository<PriceTrackerModel,Long> {

    @Query("SELECT p FROM PriceTrackerModel p " +
            "WHERE p.productScraperModel.id=:productId " +
            "AND p.priceTrackerUsers.email=:email " +
            "AND p.priceTrackerUsers.phoneNumber=:phoneNumber")
    PriceTrackerModel findExistingTracker(@Param("productId") Long productId,
                                          @Param("email") String email,
                                          @Param("phoneNumber") String phoneNumber);

    @Query("SELECT p FROM PriceTrackerModel p " +
            "WHERE p.status = :status " +
            "AND p.priceTrackerUsers.pincodeStockTracking=FALSE "+
            "AND (p.nextScrapeTime IS NULL OR p.nextScrapeTime <= :currentTime)")
    List<PriceTrackerModel> findDueProducts(@Param("status") TrackingStatus status,
                                            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT p FROM PriceTrackerModel p " +
            "WHERE p.status = :status " +
            "AND p.priceTrackerUsers.pincodeStockTracking=TRUE "+
            "AND (p.nextScrapeTime IS NULL OR p.nextScrapeTime <= :currentTime)")
    List<PriceTrackerModel> findPinCodeTrackers(@Param("status") TrackingStatus status,
                                            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT p FROM PriceTrackerModel p "+
            "WHERE p.priceTrackerUsers.email=:email " +
            "AND p.priceTrackerUsers.phoneNumber=:number ")
    List<PriceTrackerModel> findUserProducts(@Param("email") String email,
                                             @Param("number") String number);

    @Query("SELECT p from PriceTrackerModel p " +
            "WHERE p.productScraperModel.id =:productId " +
            "AND (p.priceTrackerUsers.email=:email " +
            "AND p.priceTrackerUsers.phoneNumber=:phoneNumber)")
    PriceTrackerModel findUserConfigByProductId(@Param("email") String email,
                                                @Param("phoneNumber") String phoneNumber,
                                                @Param("productId") long productId);

    @Query("SELECT p from PriceTrackerModel  p " +
            "WHERE p.priceTrackerUsers.id=:userConfigId " +
            "AND p.productScraperModel.id=:productId")
    PriceTrackerModel findByUserConfigAndProductId(@Param("userConfigId") long userConfigId,
                                                   @Param("productId")Long productId);

    @Query("SELECT p from PriceTrackerModel p " +
            "WHERE p.priceTrackerUsers.id = :id ")
    PriceTrackerModel findByPriceTrackerUser_Id(@Param("id") long id);

    @Query("SELECT p from PriceTrackerModel p " +
            "WHERE p.priceTrackerUsers.email = :email " +
            "AND p.priceTrackerUsers.phoneNumber = :phoneNumber " +
            "AND p.status = :status " +
            "AND p.priceTrackerUsers.id != :id")
    List<PriceTrackerModel> findUserTrackingProducts(@Param("email") String email,
                                                     @Param("phoneNumber") String phoneNumber,
                                                     @Param("status") TrackingStatus status,
                                                     @Param("id") long id);

//
//    @Query("SELECT p FROM PriceTrackerModel p "+
//            "WHERE p.status=:status "+
//    "AND p.critical_stock_tracking=TRUE "+
//    "AND (p.nextScrapeTime IS NULL OR p.nextScrapeTime <= :currentTime)")
//    List<PriceTrackerModel> critical_StockTracking(@Param("status") TrackingStatus status,
//                                                   @Param("currentTime") LocalDateTime currentTime);
//
//    @Query("SELECT pt FROM PriceTrackerModel pt WHERE pt.productScraperModel.id = :productId AND pt.user_email=:email")
//    PriceTrackerModel findByProductId_Email(@Param("productId") Long productId,
//                                            @Param("email") String email);
//
//    @Query("SELECT pt FROM PriceTrackerModel pt WHERE pt.productScraperModel.id = :productId AND pt.user_phone_number=:phone")
//    PriceTrackerModel findByProductId_Phone(@Param("productId") Long productId,
//                                            @Param("phone") String phone);







}
