package com.org.scraper_bkd.controller;

import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.repo.PriceTrackerUserRepo;
import com.org.scraper_bkd.service.PriceTrackerUserService;
import com.org.scraper_bkd.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/test")
@RequiredArgsConstructor
public class testingController {

    private static final Logger logger = LoggerFactory.getLogger(testingController.class);

    private final NotificationService notificationService;
    private final PriceTrackerUserRepo priceTrackerUserRepo;
    private final PriceTrackerUserService trackerUserService;


    @GetMapping("/sendStockAlertEmail")
    public ResponseEntity<String> testStockAlertEmail(){
        try {
            PriceTrackerUsers trackerUsers = priceTrackerUserRepo.findById(33L).orElse(null);
            notificationService.sendPincodeStockAlertEmail(trackerUsers, 420550);
            return ResponseEntity.ok("Email send");
        }
        catch(Exception e){
            logger.error("Error at testStockAlertEmail : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }

    }

    @GetMapping("/getPriceHistory")
    public ResponseEntity<String> testPriceHistry(){
        try{
            trackerUserService.fetchPriceHistoryInFormatted(10,47);
            return ResponseEntity.ok("price hisotry fethced send");
        }
        catch(Exception e){
            logger.error("Error at testPriceHistry {}",e.getMessage());
            return ResponseEntity.ok("error send");
        }
    }

    @GetMapping("/sayHello")
    public ResponseEntity<?> getTest(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // username (subject)
        String userId = auth.getName();
        String phoneNumber=null;
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
             phoneNumber = jwtAuth.getToken().getClaim("region");
            // ...
        }
        logger.info("authenticated user userId : {}",userId);
        logger.info("authenticated user phoneNumber : {}",phoneNumber);
        logger.info("Test API is invoked");
        return ResponseEntity.ok("Hi Dude !");
    }
}
