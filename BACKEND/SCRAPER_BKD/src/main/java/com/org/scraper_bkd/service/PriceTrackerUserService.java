package com.org.scraper_bkd.service;

/**
 * This class is responsible for managing user tracker request ,
 * notify users, modify their existed data etc.
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.org.scraper_bkd.dto.PincodeTrackerDTO;
import com.org.scraper_bkd.enums.NotificationFrequency;
import com.org.scraper_bkd.enums.TrackingStatus;
import com.org.scraper_bkd.model.PriceHistory;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.PriceHistoryRepo;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import com.org.scraper_bkd.repo.PriceTrackerUserRepo;
import com.org.scraper_bkd.service.notifications.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.org.scraper_bkd.constants.AppConstant.CLOUDINARY_URL;

@Service
@RequiredArgsConstructor
public class PriceTrackerUserService {

    private final NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(PriceTrackerUserService.class);
    private final PriceTrackerUserRepo priceTrackerUserRepo;
    private final PriceTrackerRepo priceTrackerRepo;
    private final PriceHistoryRepo priceHistoryRepo;

    private static final Cloudinary cloudinary =
            new Cloudinary(CLOUDINARY_URL);

    public boolean checkIsStockAlert(PriceTrackerUsers trackerUser, long newPrice){
        try{
            if(trackerUser.isStockAlert()){
                logger.debug("User opted to notify for stock alert");
                if(trackerUser.isPushSMSEnabled()) {
                    notificationService.checkAndSendPushNotification(trackerUser, "stock");
                }
                if(trackerUser.isEmailSMSEnabled()){
                    notificationService.sendStockAlertEmail(trackerUser,newPrice);
                }
                return true;
            }
            return false;
        }
        catch(Exception e){
            logger.error("Error at checkIsStockAlert  : {}",e.getMessage());
            return false;
        }
    }

    public boolean checkIsDefaultPriceAlert(PriceTrackerUsers trackerUser, long newPrice){
        try{
            if(trackerUser.isAutomaticAlert()){
                logger.debug("User opted to notify for automatic price alert");
                if(trackerUser.isPushSMSEnabled()) {
                    notificationService.checkAndSendPushNotification(trackerUser, "auto");
                }
                if(trackerUser.isEmailSMSEnabled()){
                    notificationService.sendAutoPriceAlert(trackerUser,newPrice);
                }
                return true;
            }
            return false;
        }
        catch(Exception e){
            logger.error("Error at checkIsDefaultPriceAlert  : {}",e.getMessage());
            return false;
        }
    }

    public boolean checkIsCustomPriceAlert(PriceTrackerUsers trackerUser, long newPrice){
        try{
            if(trackerUser.isCustomPriceAlert()){
                System.out.println("User opted to notify for custom price alert");
                if(trackerUser.isPushSMSEnabled()) {
                    notificationService.checkAndSendPushNotification(trackerUser, "customPrice");
                }
                if(trackerUser.isEmailSMSEnabled()){
                    notificationService.sendCustomPriceAlert(trackerUser,newPrice);
                }
                return true;
            }
            return false;
        }
        catch(Exception e){
            logger.error("Error at checkIsCustomPriceAlert  : {}",e.getMessage());
            return false;
        }
    }

    @Transactional
    public void checkNotificationFrequency(PriceTrackerUsers trackerUser,long newPrice) {
        try {
            if(trackerUser.isNotificationFrequencySet()) {
                LocalDateTime lastUpdated = trackerUser.getNotificationUpdateDate();
                LocalDateTime now = LocalDateTime.now();
                boolean shouldNotify = false;

                NotificationFrequency frequency = trackerUser.getNotificationFrequencyValue();
                int days=0;
                switch(frequency) {
                    case WEEKLY:
                        days=7;
                        shouldNotify = lastUpdated.plusWeeks(1).isBefore(now);
                        break;
                    case MONTHLY:
                        days=30;
                        shouldNotify = lastUpdated.plusMonths(1).isBefore(now);
                        break;
                    case CUSTOM:
                        days=trackerUser.getCustomNotificationDays();
                        shouldNotify=lastUpdated.plusDays(days).isBefore(now);
                        break;
                }

                if(shouldNotify) {
                    logger.info("Sending {} price update notification for user ID: {}",
                            frequency, trackerUser.getId());
                    PriceTrackerModel priceTrackerModel=priceTrackerRepo.findByPriceTrackerUser_Id(trackerUser.getId());
                    if(priceTrackerModel==null){
                        throw new RuntimeException("Price Tracker model can't be empty , inside function checkNotificationFrequency ");
                    }
                    ProductScraperModel scraperModel=priceTrackerModel.getProductScraperModel();

                    List<GraphGenerator.PriceEntry> priceEntryList=fetchPriceHistoryInFormatted(days,scraperModel.getId());
                    String graphName="trackerGraph"+priceTrackerModel.getId();
                    String graphPath=GraphGenerator.createChartImage(priceEntryList,600,500,graphName);
                    String graphUrl=uploadImage(graphPath);
                    List<PriceTrackerModel> userTrackingProducts=priceTrackerRepo.findUserTrackingProducts(trackerUser.getEmail(), trackerUser.getPhoneNumber(), TrackingStatus.TRACKING,trackerUser.getId());
                    List<PriceTrackerModel> topThreeList=new ArrayList<>();
                    int count=1;
                    for(PriceTrackerModel priceTrackerModel1:userTrackingProducts){
                        if(count>3){
                           break;
                        }
                        topThreeList.add(priceTrackerModel1);
                        count++;
                    }
                    notificationService.sendProductUpdateNotificationEmail(trackerUser,topThreeList,newPrice,frequency,graphUrl);


                    // Update the last notification time
                    trackerUser.setNotificationUpdateDate(now);
                    priceTrackerUserRepo.save(trackerUser);

                    //Now delete the temp graph image
                    if (graphPath != null) {
                        Path p = Path.of(graphPath);

                        try {
                            Files.deleteIfExists(p);        // returns true if it actually deleted
                            logger.debug("Temp file {} removed", p);
                        } catch (IOException ex) {
                            // Not fatal, but worth logging
                            logger.warn("Could not delete temp file {}", p, ex);
                        }
                    }
                }
            }
            else{
                logger.info("Tracker user haven't enables product update notification");
            }
        } catch(Exception e) {
            logger.error("Error at checkNotificationFrequency: {}", e.getMessage());
        }
    }

    public void notifyUserPincodeStatus(PriceTrackerModel product, PincodeTrackerDTO trackerDTO) {
        try{
            ProductScraperModel productScraperModel = product.getProductScraperModel();
            PriceTrackerUsers trackerUser = product.getPriceTrackerUsers();
            if(trackerUser.isPincodeStockTracking()){
                if(trackerUser.isPushSMSEnabled()) {
                    notificationService.checkAndSendPushNotification(trackerUser, "pincode");
                }
                if(trackerUser.isEmailSMSEnabled()){
                    notificationService.sendPincodeStockAlertEmail(trackerUser);
                }
            }
        }
        catch(Exception e){
            logger.error("Error while notifying user of pincode status {}",e.getMessage());
        }
    }

    @Transactional
    public List<GraphGenerator.PriceEntry> fetchPriceHistoryInFormatted(int days,long product_id){

        try {
            LocalDateTime startingDate = LocalDateTime.now().minusDays(days);

            List<PriceHistory> priceHistoryList = priceHistoryRepo.findByProductIdAndDate(product_id, startingDate);
            List<GraphGenerator.PriceEntry> priceEntryList=new ArrayList<>();
            for(PriceHistory priceHistory:priceHistoryList){
                long price=priceHistory.getPrice();
                LocalDateTime date=priceHistory.getTimestamp();
                int day=date.getDayOfMonth();
                priceEntryList.add(new GraphGenerator.PriceEntry(price,day));
            }
            System.out.println(priceEntryList);
            return priceEntryList;

        }
        catch(Exception e){
            logger.error("Error at fetchPriceHistoryInFormatted : {}",e.getMessage());
            return new ArrayList<>();
        }
    }

    //This function uploads the image to Cloudinary Server
    public String uploadImage(String path) throws IOException {

        Map<String, Object> options = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false          // keep existing assets safe
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> result =
                cloudinary.uploader().upload(new File(path), options);

        // secure_url is always https; fallback to url if null
        return (String) result.getOrDefault("secure_url", result.get("url"));
    }

    @Transactional
    public void testing() throws IOException {
        PriceTrackerUsers trackerUsers=priceTrackerUserRepo.findById(31L).orElse(null);

        checkNotificationFrequency(trackerUsers,200L);




    }
}
