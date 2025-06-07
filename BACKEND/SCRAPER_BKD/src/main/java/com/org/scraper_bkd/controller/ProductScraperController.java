package com.org.scraper_bkd.controller;

import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.service.ProductScraperService;
import com.org.scraper_bkd.service.notifications.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductScraperController
{
    private static final Logger logger = LoggerFactory.getLogger(ProductScraperService.class);

    private final ProductScraperService productScraperService;
    private final SmsService smsService;




    @PostMapping("/fetchProduct")
    public ResponseEntity<?> fetchProductDetails(@RequestBody Map<String, String> requestBody) {
        try {
            String url = requestBody.get("url");
            if (url == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing url");
            }

            ProductScraperModel productScraperModel = productScraperService.getProductDetails(url);
            if (productScraperModel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Product Not found");
            }
            return ResponseEntity.ok(productScraperModel);
        }
        catch (Exception e) {
            logger.error("Error while fetching product at /fetchProduct : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch product");
        }
    }



}
