package com.org.scraper_bkd.dto;

import com.org.scraper_bkd.enums.TrackingStatus;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.utils.ProductHelper;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardProductsDTO {

    //id of price tracker
    long id;

    String brand;

    String productURL;

    String productName;
    long price;
    long mrp;
    String currency;

    float ratings;
    int reviews;
    String stock_status;

    String imageURL;

    @Enumerated(EnumType.STRING)
    TrackingStatus running_status ;

    Map<String,Boolean> userTargetStatus;


    public  DashboardProductsDTO(PriceTrackerModel priceTrackerModel){
        ProductScraperModel product = priceTrackerModel.getProductScraperModel();
        PriceTrackerUsers user = priceTrackerModel.getPriceTrackerUsers();

        this.setId(priceTrackerModel.getId());
        this.setBrand(product.getBrand());
        this.setProductURL(product.getProductURL());
        this.setProductName(product.getProductName());
        this.setPrice(product.getPrice());
        this.setMrp(product.getMrp());
        this.setCurrency(product.getCurrency());
        this.setRatings(product.getRatings());
        this.setReviews(product.getReviews());
        this.setStock_status(product.getStock_status());
        this.setImageURL(product.getImageURL());
        this.setRunning_status(priceTrackerModel.getStatus());

        // Add fields from PriceTrackerUsers if needed
        Map<String,Boolean> userTrackerTarget=new HashMap<>();
        if(user.isCustomPriceAlert()){
            String currency= ProductHelper.getCurrencySymbol(product.getCurrency());
            userTrackerTarget.put(currency + user.getCustomPrice(),user.isCustomPriceAlertSuccess());
        }
        if(user.isAutomaticAlert()){
            userTrackerTarget.put("AUTO",user.isAutomaticAlertSuccess());
        }
        if(user.isStockAlert()){
            userTrackerTarget.put("STOCK_ALERT", product.getStock_status().equalsIgnoreCase("in_stock"));
        }
        if(user.isPincodeStockTracking()){
            userTrackerTarget.put("PINCODE_ALERT",user.isPincodeAlertSuccess());
        }
        this.setUserTargetStatus(userTrackerTarget);
    }
}
