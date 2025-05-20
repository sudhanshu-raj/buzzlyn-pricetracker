package com.org.scraper_bkd.service;

/**
 * This class is responsible for running 24x7 to fetch the products details ,
 * functions, it runs at every particular interval we can say most important class for our need
 */

import com.org.scraper_bkd.dto.PincodeTrackerDTO;
import com.org.scraper_bkd.dto.PriceScraperDTO;
import com.org.scraper_bkd.enums.TrackingStatus;
import com.org.scraper_bkd.exception.BadClient_Request;
import com.org.scraper_bkd.exception.InvalidPythonAPI_Request;
import com.org.scraper_bkd.model.PriceHistory;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.PriceHistoryRepo;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import com.org.scraper_bkd.repo.PriceTrackerUserRepo;
import com.org.scraper_bkd.repo.ProductScraperRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceTrackingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PriceTrackingScheduler.class);

    private final PriceTrackerRepo priceTrackerRepo;
    private final PriceHistoryRepo priceHistoryRepo;
    private  final PriceTrackerService priceTrackerService;
    private final ProductScraperService  scraperService;
    private final PriceTrackerUserService trackerUserService;
    private final ProductScraperRepo productScraperRepo;
    private final PriceTrackerUserRepo priceTrackerUserRepo;

    @Scheduled(fixedRate = 14400000)  // 4 hour , it is in milliseconds
    @Transactional
    public void trackPrices() {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTrackerModel>  prices_to_track=priceTrackerRepo.findDueProducts(TrackingStatus.TRACKING,now);
        logger.info("Found {} due products for price checking at {}", prices_to_track.size(), now);

        for (PriceTrackerModel product : prices_to_track) {
            // Offload processing to an asynchronous task
            processProductAsync(product);
        }
    }

    @Scheduled(fixedRate = 14400000) // 4 hours
    public void pincodeTrackers(){
        LocalDateTime now = LocalDateTime.now();
        List<PriceTrackerModel> pincodeTrackerProducts=priceTrackerRepo.findPinCodeTrackers(TrackingStatus.TRACKING,now);
        logger.info("Found {} pincode trackers , processing at {}", pincodeTrackerProducts.size(), now );
        for (PriceTrackerModel product : pincodeTrackerProducts) {
            processPincodeTrackers(product);
        }

    }

    /*
    This tracks the products which is generally not in any user dashboard
    which means either user price tracking stopped or they deleted
    but we have to track all products right , so that's why if no user is currently
    tracking particular product then still track those as it's important for price history data
     */
    @Scheduled(fixedRate = 14400000)  // 4hours,  it is in milliseconds
    public void processNonTrackingProducts(){
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime timeThreshold = now.minusHours(4);  // 4 hours
        List<ProductScraperModel> nonTrackedProducts= productScraperRepo.findNonTrackingProducts(timeThreshold);
        logger.info("Found {}  non tracked products, processing them at {}",nonTrackedProducts.size(),now);
        for(ProductScraperModel product:nonTrackedProducts){
            processNonTrackProducts(product);
        }
    }

    @Async("taskExecutor")
    public void processPincodeTrackers(PriceTrackerModel product) {
        try{
            ProductScraperModel productScraperModel = product.getProductScraperModel();
            PriceTrackerUsers trackerUser = product.getPriceTrackerUsers();
            logger.info("Processing product pincode tracker asynchronously of product: {}", productScraperModel.getId());

            String url=productScraperModel.getProductURL();
            String pincode= trackerUser.getPincode();
            PincodeTrackerDTO trackerDTO =priceTrackerService.fetchPincodeTracker(url,pincode);
            if(trackerDTO!=null && trackerDTO.isStockAvailable()){
                String deliveryDate=trackerDTO.getDeliveryDate();
                trackerUserService.notifyUserPincodeStatus(product,trackerDTO);
                if((trackerUser.isAutomaticAlert() && trackerUser.isAutomaticAlertSuccess()) ||
                        (trackerUser.isCustomPriceAlert() && trackerUser.isCustomPriceAlertSuccess())) {
                    product.setStatus(TrackingStatus.STOPPED);
                }
                trackerUser.setPincodeAlertSuccess(true);
                priceTrackerUserRepo.save(trackerUser);
            }
            else{
                product.setNextScrapeTime(LocalDateTime.now().plusHours(4));  // 4 hours
            }
            logger.info("Finished processing product pincode tracker asynchronously of product: {}", productScraperModel.getId());
        }
        catch(InvalidPythonAPI_Request | BadClient_Request e){
            logger.error("Error while processing pincode tracker for scraper id : {} , error : {}",
                    product.getId(),e.getMessage());
        }
        catch(Exception e){
            logger.error("Unexpected error while processing pincode tracker for scraper id : {} , error : {}",
                    product.getId(),e.getMessage());
        }
    }

    /*
    This process the user products based in price alert, that they added for tracking
     */
    @Async("taskExecutor")
    public void processProductAsync(PriceTrackerModel product) {
        try {
            ProductScraperModel productScraperModel = product.getProductScraperModel();
            PriceTrackerUsers trackerUser = product.getPriceTrackerUsers();

            logger.info("Processing product asynchronously for the product id : {}", productScraperModel.getId());
            PriceScraperDTO productDetails = priceTrackerService.fetchPriceScraper_API(productScraperModel.getProductURL());
            productDetails.setTracking_status(true);  // means this product is in tracking already by user, right
            if (productDetails == null) {
                logger.warn("Failed to fetch price under scheduler for product id : {}", productScraperModel.getId());
                return;

            }
            long newPrice = productDetails.getPrice();
            if (newPrice <= 0) {
                logger.warn("Failed to fetch price under scheduler for product id : {}", productScraperModel.getId());
                return;
            }


            if (!(productScraperModel.getStock_status().equalsIgnoreCase(productDetails.getStock()))) {
                logger.info("Changed in  Product stock tracking for product id: {}  ", productScraperModel.getId());
                if (trackerUserService.checkIsStockAlert(trackerUser,newPrice)) {
                    product.setStatus(TrackingStatus.STOPPED);
                    productDetails.setTracking_status(false);
                }
            }

            long user_custom_price = trackerUser.getCustomPrice();
            if (user_custom_price != 0 && (user_custom_price == newPrice)) {
                logger.info("Change in user custom price of product id : {}", productScraperModel.getId());
                if (trackerUserService.checkIsCustomPriceAlert(trackerUser,newPrice)) {
                    //check if user has added pincode tracking too , and if it success also then only stopped the tracking status
                    boolean shouldStopTracking = !trackerUser.isPincodeStockTracking() ||
                            trackerUser.isPincodeAlertSuccess();

                    if (shouldStopTracking) {
                        product.setStatus(TrackingStatus.STOPPED);
                        productDetails.setTracking_status(false);
                    }
                    trackerUser.setCustomPriceAlertSuccess(true);
                }
            } else if (product.getFirst_time_price() != 0 && (product.getFirst_time_price() > newPrice)) {
                logger.info("Product got the lower price id : {}", product.getId());
                if (trackerUserService.checkIsDefaultPriceAlert(trackerUser,newPrice)) {

                    boolean shouldStopTracking = !trackerUser.isPincodeStockTracking() ||
                            trackerUser.isPincodeAlertSuccess();

                    if (shouldStopTracking) {
                        product.setStatus(TrackingStatus.STOPPED);
                        productDetails.setTracking_status(false);
                    }
                    trackerUser.setAutomaticAlertSuccess(true);
                }
            } else {
                product.setNextScrapeTime(LocalDateTime.now().plusHours(4));  //4 hours
            }
            //also check if there is notification frequency asked by user
            trackerUserService.checkNotificationFrequency(trackerUser,newPrice);

            product.setLastChecked(LocalDateTime.now());
            product.setCurrent_time_price(newPrice);

            PriceHistory history = new PriceHistory();
            history.setProductScraperModel(productScraperModel);
            history.setPrice(newPrice);
            history.setTimestamp(LocalDateTime.now());
            priceHistoryRepo.save(history);
            priceTrackerRepo.save(product);
            priceTrackerUserRepo.save(trackerUser);
            scraperService.updateProductData(productDetails, productScraperModel.getId(),false);
            logger.info("Finished processing product asynchronously for the product id : {}", productScraperModel.getId());
        }
        catch(InvalidPythonAPI_Request | BadClient_Request e){
            logger.error("Error while processing the scheduler product of tracker id : {} , error :{}",
                    product.getId(),e.getMessage());
        }
        catch(Exception e){
            logger.error("Unexpected error while processing the scheduler product of tracker id : {} , error :{}",
                    product.getId(),e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void processNonTrackProducts(ProductScraperModel productScraperModel){

        try {
            logger.info("Processing non-tracking products asynchronously for the product id: {}", productScraperModel.getId());
            PriceScraperDTO productDetails = priceTrackerService.fetchPriceScraper_API(productScraperModel.getProductURL());

            if (productDetails == null) {
                logger.warn("Failed to fetch price under scheduler for product id,reason null data : {}", productScraperModel.getId());
                return;

            }
            long newPrice = productDetails.getPrice();
            if (newPrice <= 0) {
                logger.warn("Failed to fetch price under scheduler for product id, reason price less than 0 : {}", productScraperModel.getId());
                return;
            }
            scraperService.updateProductData(productDetails, productScraperModel.getId(),true);

            PriceHistory history = new PriceHistory();
            history.setProductScraperModel(productScraperModel);
            history.setPrice(newPrice);
            history.setTimestamp(LocalDateTime.now());
            priceHistoryRepo.save(history);
            logger.info("Finished processing non-tracking products asynchronously for the product id: {}", productScraperModel.getId());
        }
        catch(Exception e){
            logger.error("Error occurred at processNonTrackProducts : {} ",e.getMessage());
        }

    }

//    @Async("taskExecutor")
//    public void processCritical_StockTracking(PriceTrackerModel product) {
//        ProductScraperModel productScraperModel=product.getProductScraperModel();
//        logger.info("Processing product: {} , asynchronously for critical stock tracking", productScraperModel.getId());
//        PriceScraperDTO productDetails =priceTrackerService.fetchPriceScraper_API(productScraperModel.getProductURL());
//        if (productDetails == null) {
//            logger.error("Failed to fetch price under scheduler for product: {}", productScraperModel.getId());
//            return;
//        }
//        product.setLastChecked(LocalDateTime.now());
//        if(!productScraperModel.getStock_status().equalsIgnoreCase(productDetails.getStock())){
//            logger.info("Critical stock tracking for product:{} is  done", productScraperModel.getId());
//            product.setStatus(TrackingStatus.STOPPED);
//        }
//        else {
//            product.setNextScrapeTime(LocalDateTime.now().plusMinutes(30));
//        }
//        priceTrackerRepo.save(product);
//
//    }
}
