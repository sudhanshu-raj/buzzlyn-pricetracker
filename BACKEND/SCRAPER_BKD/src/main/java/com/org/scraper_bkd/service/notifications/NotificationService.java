package com.org.scraper_bkd.service.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.scraper_bkd.enums.NotificationFrequency;
import com.org.scraper_bkd.model.*;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import com.org.scraper_bkd.repo.ProductScraperRepo;
import com.org.scraper_bkd.repo.SubscriptionRepository;
import com.org.scraper_bkd.utils.ProductHelper;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.org.scraper_bkd.constants.AppConstant.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PriceTrackerRepo priceTrackerRepo;
    private final ProductScraperRepo productScraperRepo;
    private final ObjectMapper objectMapper;


    public void checkAndSendPushNotification(PriceTrackerUsers priceTrackerUser,String notificationPurpose) throws GeneralSecurityException {
        try {
                List<WebPushSubscription> webPushSubscriptionList = subscriptionRepository.findByEmailAndPhoneNumber(priceTrackerUser.getEmail(), priceTrackerUser.getPhoneNumber());
                PriceTrackerModel priceTrackerModel = priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
                if (priceTrackerModel != null) {
                    ProductScraperModel productScraperModel = priceTrackerModel.getProductScraperModel();
                    String productName = productScraperModel.getProductName().substring(0, 51);
                    String brand = productScraperModel.getBrand();
                    brand = brand.split("\\.")[0].toUpperCase();
                    String title = null;
                    String message = null;
                    if (notificationPurpose.equalsIgnoreCase("stock")) {
                        title = brand + " Stock Alert";
                        message = productName + ", back in stock. Check out now.";
                    } else if (notificationPurpose.equalsIgnoreCase("auto")) {
                        title = brand + " Price Alert";
                        message = productName + ", price is dropped. Don't miss it.";
                    } else if (notificationPurpose.equalsIgnoreCase("customPrice")) {
                        title = brand + " Price Alert";
                        message = productName + ", price dropped at your custom price.";
                    } else if (notificationPurpose.equalsIgnoreCase("pincode")) {
                        title = brand + " Stock Alert";
                        message = productName + ", back in stock at your pincode";
                    }
                    String clickTarget = productScraperModel.getProductURL();
                    String imageUrl = productScraperModel.getImageURL();
                    String icon = "https://play-lh.googleusercontent.com/C1ia9xQR3lrOBl0YDGpD0o5kx5-NfiGwwwM4VF0Xk2sVwP2QhJCVU5b4rFzlZobcmg=w480-h960-rw";
                    WebPushMessage webPushMessage = new WebPushMessage(title, message, clickTarget, icon, imageUrl);
                    PushService pushService = new PushService(WEBPUSH_PUBLIC_KEY, WEBPUSH_PRIVATE_KEY, WEBPUSH_MAIL);

                    int successCount = 0;
                    int failCount = 0;
                    for (WebPushSubscription subscription : webPushSubscriptionList) {
                        try {
                            // Validate subscription data before creating notification
                            if (subscription.getNotificationEndPoint() == null ||
                                    subscription.getPublicKey() == null ||
                                    subscription.getAuth() == null) {

                                logger.warn("Skipping invalid subscription: missing required fields. ID: {}",
                                        subscription.getId());
                                failCount++;
                                continue;
                            }

                            Notification notification = new Notification(
                                    subscription.getNotificationEndPoint(),
                                    subscription.getPublicKey(),
                                    subscription.getAuth(),
                                    objectMapper.writeValueAsBytes(webPushMessage)
                            );

                            pushService.send(notification);
                            successCount++;
                        } catch (Exception e) {
                            logger.error("Failed to send notification to subscription ID {}: {}",
                                    subscription.getId(), e.getMessage());
                            failCount++;
                        }

                        logger.info("Push notifications summary for {} user config id , {} successful, {} failed", priceTrackerUser.getId(), successCount, failCount);
                    }
                }
                else{
                    logger.warn("Trying to send push notification but price tracker not found ");
                }
        } catch (Exception e) {
            logger.error("Error at checkAndSendPushNotification : {}", e.getMessage());
        }
    }


    public void  sendStockAlertEmail(PriceTrackerUsers  priceTrackerUser,long newPrice){
        try{
            if(priceTrackerUser==null){
                logger.warn("Trying to send stock email alert on empty price tracker user model");
                return;
            }
            PriceTrackerModel priceTrackerModel = priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
            if(priceTrackerModel==null){
                logger.warn("Trying to send stock alert email but price tracker model is empty ");
                return;
            }
            ProductScraperModel scraperModel = priceTrackerModel.getProductScraperModel();
            if(scraperModel==null){
                logger.warn("Trying to send stock alert email but product scraper model is empty ");
                return;
            }
            String email = priceTrackerUser.getEmail();
            String brand = scraperModel.getBrand().split("\\.")[0];
            String productNameMod=scraperModel.getProductName().substring(0,35)+" ...";
            brand = brand.isEmpty() ? "" : Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
            String subject =brand+" Stock Alert | "+productNameMod;
            String rawTemplate=EmailService.loadTemplate("stockAlert.template");
            String subjectPreview="🎉 Back in Stock! The item on your tracking list is now available for purchase.";

            String finalTemplate=rawTemplate
                    .replace("{logoUrl}",BUZZLYN_LOGO)
                    .replace("{brand}", brand.toLowerCase(Locale.ROOT))
                    .replace("{productName}", scraperModel.getProductName())
                    .replace("{newPrice}", Objects.requireNonNull(ProductHelper.formatPrice(newPrice, scraperModel.getCurrency())))
                    .replace("{currencySymbol}",CURRENCY_SYMBOLS.get(scraperModel.getCurrency()))
                    .replace("{productImageUrl}", scraperModel.getImageURL())
                    .replace("{productUrl}", scraperModel.getProductURL())
                            .replace("{subject}",subjectPreview);
            EmailService.sendEmail(email,subject,finalTemplate);
        }
        catch(Exception e){
            logger.error("Error while sending email for stock alert : {}",e.getMessage());
        }
    }

    //for emails
    public void sendAutoPriceAlert(PriceTrackerUsers priceTrackerUser,long newPrice){
        try{
            if(priceTrackerUser==null){
                logger.warn("Trying to send auto price dropped alert email alert on empty price tracker user model");
                return;
            }
            PriceTrackerModel priceTrackerModel = priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
            if(priceTrackerModel==null){
                logger.warn("Trying to send auto price dropped alert  email but price tracker model is empty ");
                return;
            }
            ProductScraperModel scraperModel = priceTrackerModel.getProductScraperModel();
            if(scraperModel==null){
                logger.warn("Trying to send auto price dropped alert email but product scraper model is empty ");
                return;
            }

            String email = priceTrackerUser.getEmail();
            String brand = scraperModel.getBrand().split("\\.")[0];
            String productNameMod=scraperModel.getProductName().substring(0,35)+" ...";
            brand = brand.isEmpty() ? "" : Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
            String subject =brand+" Price Drop Alert | "+productNameMod;

            String newPrice_formatted= ProductHelper.formatPrice(newPrice,scraperModel.getCurrency());
            String newPrice_;
            if(newPrice_formatted!=null){
                newPrice_=newPrice_formatted;
            }
            else{
                newPrice_= String.valueOf(newPrice);
            }

            String oldPrice_formatted= ProductHelper.formatPrice(newPrice,scraperModel.getCurrency());
            String oldPrice;
            if(oldPrice_formatted!=null){
                oldPrice=ProductHelper.formatPrice(priceTrackerModel.getFirst_time_price(),scraperModel.getCurrency());
            }
            else{
                oldPrice= String.valueOf(priceTrackerModel.getFirst_time_price());
            }

            int savingPercentageValue = 0;
            if (priceTrackerModel.getFirst_time_price() > 0) {
                savingPercentageValue = (int) Math.round(((double)(priceTrackerModel.getFirst_time_price() - newPrice) /
                        priceTrackerModel.getFirst_time_price()) * 100);
            }
            String subjectPreview="🎉 Price Drop Alert! The price of the product you're tracking has dropped!";
            String rawTemplate=EmailService.loadTemplate("autoPriceAlert.template");

            String finalTemplate=rawTemplate
                    .replace("{logoUrl}",BUZZLYN_LOGO)
                    .replace("{brand}", brand.toLowerCase(Locale.ROOT))
                    .replace("{productName}", scraperModel.getProductName())
                    .replace("{newPrice}", newPrice_ )
                    .replace("{oldPrice}",oldPrice!=null?oldPrice:"")
                    .replace("{currencySymbol}",CURRENCY_SYMBOLS.get(scraperModel.getCurrency()))
                    .replace("{savings}",String.valueOf(priceTrackerModel.getFirst_time_price()-newPrice))
                    .replace("{savingsPercentage}",String.valueOf(savingPercentageValue))
                    .replace("{productImageUrl}", scraperModel.getImageURL())
                    .replace("{productUrl}", scraperModel.getProductURL())
                            .replace("{subject}",subjectPreview);
            EmailService.sendEmail(email,subject,finalTemplate);
        }
        catch(Exception e){
            logger.error("Error while sending email for auto price alert : {}",e.getMessage());
        }
    }

    //for emails
    public void sendCustomPriceAlert(PriceTrackerUsers priceTrackerUser,long newPrice){
        try{
            if(priceTrackerUser==null){
                logger.warn("Trying to send custom price dropped alert email alert on empty price tracker user model");
                return;
            }
            PriceTrackerModel priceTrackerModel = priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
            if(priceTrackerModel==null){
                logger.warn("Trying to send custom price dropped alert  email but price tracker model is empty ");
                return;
            }
            ProductScraperModel scraperModel = priceTrackerModel.getProductScraperModel();
            if(scraperModel==null){
                logger.warn("Trying to send custom price dropped alert email but product scraper model is empty ");
                return;
            }

            String email = priceTrackerUser.getEmail();
            String brand = scraperModel.getBrand().split("\\.")[0];
            String productNameMod=scraperModel.getProductName().substring(0,35)+" ...";
            brand = brand.isEmpty() ? "" : Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
            String subject =brand+" Price Matched | "+productNameMod;

            String newPrice_formatted= ProductHelper.formatPrice(newPrice,scraperModel.getCurrency());
            String newPrice_;
            if(newPrice_formatted!=null){
                newPrice_=newPrice_formatted;
            }
            else{
                newPrice_= String.valueOf(newPrice);
            }

            String oldPrice_formatted= ProductHelper.formatPrice(newPrice,scraperModel.getCurrency());
            String oldPrice;
            if(oldPrice_formatted!=null){
                oldPrice=ProductHelper.formatPrice(priceTrackerModel.getFirst_time_price(),scraperModel.getCurrency());
            }
            else{
                oldPrice= String.valueOf(priceTrackerModel.getFirst_time_price());
            }

            String rawTemplate=EmailService.loadTemplate("customPriceAlert.template");
            String subjectPreview=" 🎯 Price Matched! Your Deal Is Live! The price of the product you're tracking now matches the price you set";

            String finalTemplate=rawTemplate
                    .replace("{logoUrl}",BUZZLYN_LOGO)
                    .replace("{brand}", brand.toLowerCase(Locale.ROOT))
                    .replace("{productName}", scraperModel.getProductName())
                    .replace("{newPrice}", newPrice_ )
                    .replace("{oldPrice}",oldPrice!=null?oldPrice:"")
                    .replace("{currencySymbol}",CURRENCY_SYMBOLS.get(scraperModel.getCurrency()))
                    .replace("{productImageUrl}", scraperModel.getImageURL())
                    .replace("{productUrl}", scraperModel.getProductURL())
                            .replace("{subject}",subjectPreview);
            EmailService.sendEmail(email,subject,finalTemplate);
        }
        catch(Exception e){
            logger.error("Error while sending email for custom price alert : {}",e.getMessage());
        }
    }

    public void  sendPincodeStockAlertEmail(PriceTrackerUsers  priceTrackerUser){
        try{
            if(priceTrackerUser==null){
                logger.warn("Trying to send pincode stock email alert on empty price tracker user model");
                return;
            }
            PriceTrackerModel priceTrackerModel = priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
            if(priceTrackerModel==null){
                logger.warn("Trying to send pincode stock alert email but price tracker model is empty ");
                return;
            }
            ProductScraperModel scraperModel = priceTrackerModel.getProductScraperModel();
            if(scraperModel==null){
                logger.warn("Trying to send pincode stock alert email but product scraper model is empty ");
                return;
            }
            String email = priceTrackerUser.getEmail();
            String brand = scraperModel.getBrand().split("\\.")[0];
            String productNameMod=scraperModel.getProductName().substring(0,35)+" ...";
            brand = brand.isEmpty() ? "" : Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
            String subject =brand+" Pincode Stock Alert | "+productNameMod;
            String rawTemplate=EmailService.loadTemplate("pincodeStockAlert.template");
            String subjectPreview="🎉 Back in Stock! The item on your tracking list is now available for purchase at your pincode";

            String finalTemplate=rawTemplate
                    .replace("{logoUrl}",BUZZLYN_LOGO)
                    .replace("{brand}", brand.toLowerCase(Locale.ROOT))
                    .replace("{productName}", scraperModel.getProductName())
                    .replace("{productImageUrl}", scraperModel.getImageURL())
                    .replace("{productUrl}", scraperModel.getProductURL())
                    .replace("{subject}",subjectPreview)
                    .replace("{pincode}",priceTrackerUser.getPincode());
            EmailService.sendEmail(email,subject,finalTemplate);
        }
        catch(Exception e){
            logger.error("Error while sending email for pincode stock alert : {}",e.getMessage());
        }
    }

    public  void sendProductUpdateNotificationEmail(PriceTrackerUsers priceTrackerUser, List<PriceTrackerModel> priceTrackerModelList, long newPrice, NotificationFrequency frequency, String graphImageUrl) throws IOException {
        PriceTrackerModel priceTrackerModel = null;
        try {
            if (priceTrackerUser == null) {
                return;
            }
            priceTrackerModel = priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
            if (priceTrackerModel == null) {
                return;
            }
            ProductScraperModel scraperModel = priceTrackerModel.getProductScraperModel();
            if (scraperModel == null) {
                return;
            }

            String productCardTemplate = EmailService.loadTemplate("productUpdates_ProductCard.template");
            List<String> productCardsList = new ArrayList<>();

            for (PriceTrackerModel priceTrackerModel1 : priceTrackerModelList) {

                ProductScraperModel productScraperModel = priceTrackerModel1.getProductScraperModel();

                String brand = productScraperModel.getBrand().split("\\.")[0];
                String productNameMod = productScraperModel.getProductName().substring(0, 30) + " ...";
                long oldPrice = priceTrackerModel1.getFirst_time_price();
                long newPrice1 = productScraperModel.getPrice();
                String currency = CURRENCY_SYMBOLS.get(productScraperModel.getCurrency());

                String productPriceStatus = null;
                if (oldPrice > newPrice1) {
                    productPriceStatus = "🔻 Price dropped from " + currency + oldPrice + " to " + currency + newPrice1;
                } else if (oldPrice < newPrice1){
                    productPriceStatus = "🔼 Price increased from " + currency + oldPrice + " to " + currency + newPrice1;
                }
                else{
                    productPriceStatus="↕️ No Price Changed";
                }

                String finalProductCard = productCardTemplate
                        .replace("{productImageUrl}", productScraperModel.getImageURL())
                        .replace("{productBrand}", brand)
                        .replace("{productName}", productNameMod)
                        .replace("{productNewPrice}", currency + " " + newPrice1)
                        .replace("{productPriceStatus}", productPriceStatus)
                        .replace("{productStockStatus}", productScraperModel.getStock_status().equalsIgnoreCase("in_stock") ? "In Stock" : "Out Stock")
                        .replace("{productUrl}", productScraperModel.getProductURL());
                productCardsList.add(finalProductCard);
            }


            String htmlTemplate = EmailService.loadTemplate("productUpdates.template");

            String email = priceTrackerUser.getEmail();
            String brand = scraperModel.getBrand().split("\\.")[0];
            String productNameMod = scraperModel.getProductName().substring(0, 35) + " ...";
            brand = brand.isEmpty() ? "" : Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
            String subject = brand + " Product Updates of " + productNameMod;
            String emailListSubject= frequency==NotificationFrequency.CUSTOM ? "Your":frequency.name().toLowerCase(Locale.ROOT)+" Product Updates 📰";
            String currency = CURRENCY_SYMBOLS.get(scraperModel.getCurrency());

            String productPriceStatus = null;
            if (priceTrackerModel.getFirst_time_price() > newPrice) {
                productPriceStatus = "🔻 Price dropped from " + currency + priceTrackerModel.getFirst_time_price() + " to " + currency + newPrice;
            } else if (priceTrackerModel.getFirst_time_price() < newPrice){
                productPriceStatus = "🔼 Price increased from " + currency + priceTrackerModel.getFirst_time_price() + " to " + currency + newPrice;
            }
            else{
                productPriceStatus="↕️ No Price Changed ";
            }

            // Safely get product cards or use empty strings
            String product1 = productCardsList.size() > 0 ? productCardsList.get(0) : "";
            String product2 = productCardsList.size() > 1 ? productCardsList.get(1) : "";
            String product3 = productCardsList.size() > 2 ? productCardsList.get(2) : "";

            String finalHtml = htmlTemplate
                    // Values from the Java example
                    .replace("{logoUrl}", BUZZLYN_LOGO)
                    .replace("{brand}", "amazon")
                    .replace("{timePeriod}",frequency==NotificationFrequency.CUSTOM ? "Your":frequency.name().toLowerCase(Locale.ROOT))
                    .replace("{productName}", productNameMod)
                    .replace("{newPriceOnly}", String.valueOf(scraperModel.getPrice()))
                    .replace("{currencySymbol}", currency)
                    .replace("{dashboardUrl}", DASHBOARD_URL)
                    .replace("{stockStatus}", scraperModel.getStock_status().equalsIgnoreCase("in_stock") ? "In Stock" : "Out Stock")
                    .replace("{productImageUrl}", scraperModel.getImageURL())
                    .replace("{mainProductUrl}", scraperModel.getProductURL())
                    .replace("{priceStatus}", productPriceStatus)
                    .replace("{graph}", graphImageUrl)
                    .replace("{subject}", emailListSubject)
                    .replace("<!--otherProduct-->", productCardsList.isEmpty() ? "" : "Other Products")
                    .replace("<!--product1-->", product1)
                    .replace("<!--product2-->", product2)
                    .replace("<!--product3-->", product3);

            EmailService.sendEmail(email, subject, finalHtml);
            logger.info("Product updates email send successfully for tracker id : {}", priceTrackerModel.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error while sending product updates notification email for tracker id : "+ priceTrackerModel.getId() +" error : "+e.getMessage());
        }
    }


}
