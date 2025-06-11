package com.org.scraper_bkd.service.notifications;

import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import com.org.scraper_bkd.service.PriceTrackerService;
import com.org.scraper_bkd.service.ProductScraperService;
import com.org.scraper_bkd.utils.ProductHelper;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private final TwilioSMS twilioSMS;
    private final PriceTrackerRepo priceTrackerRepo;


    public void priceAlertWBSMS(PriceTrackerUsers trackerUser, long newPrice){
        try{
            String to=trackerUser.getPhoneNumber();
            PriceTrackerModel priceTrackerModel=priceTrackerRepo.findByPriceTrackerUser_Id(trackerUser.getId());
            if(priceTrackerModel==null){
                logger.warn("Can't send the price alert sms because price tracker data not found for pricetrackeruser id : {}",trackerUser.getId());
                return;
            }
            ProductScraperModel scraperModel=priceTrackerModel.getProductScraperModel();
            String product_name= scraperModel.getProductName();
            if(product_name.length()>50){
                product_name=product_name.substring(0,50)+"...";
            }
            String brand= scraperModel.getBrand();
            String currency_code= scraperModel.getCurrency();
            String currency_symbol= ProductHelper.getCurrencySymbol(currency_code);
            String old_price=currency_symbol+" "+ProductHelper.formatPrice(priceTrackerModel.getFirst_time_price(),currency_code);
            String new_price=currency_symbol+" "+ProductHelper.formatPrice(newPrice,currency_code);
            String product_url= scraperModel.getProductURL();
            String product_image= scraperModel.getImageURL();

            twilioSMS.sendPriceAlertSMS(to,product_name,brand,old_price,new_price,product_url,product_image);

        }
        catch(Exception e){
            logger.error("Error at priceAlertWBSMS : {}",e.getMessage());
        }
    }

    public void stockAlertWBSMS(PriceTrackerUsers trackerUser){
        try{
            PriceTrackerModel priceTrackerModel=priceTrackerRepo.findByPriceTrackerUser_Id(trackerUser.getId());
            if(priceTrackerModel==null){
                logger.warn("Can't send the stock  alert sms because price tracker data not found for pricetrackeruser id : {}",trackerUser.getId());
                return;
            }

        ProductScraperModel scraperModel=priceTrackerModel.getProductScraperModel();
            String to=trackerUser.getPhoneNumber();
            String product_name= scraperModel.getProductName();
            if(product_name.length()>50){
                product_name=product_name.substring(0,50)+"...";
            }
            String product_url= scraperModel.getProductURL();
            String product_image= scraperModel.getImageURL();

            twilioSMS.sendStockAlertSMS(to,product_name,product_url,product_image);
        }
        catch(Exception e){
            logger.error("Error at stockAlertWBSMS : {}",e.getMessage());
        }
    }



}
