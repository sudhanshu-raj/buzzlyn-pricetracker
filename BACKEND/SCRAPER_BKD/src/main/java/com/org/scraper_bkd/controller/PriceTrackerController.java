package com.org.scraper_bkd.controller;

import com.org.scraper_bkd.dto.DashboardProductsDTO;
import com.org.scraper_bkd.dto.PriceTrackerRequest;
import com.org.scraper_bkd.dto.UserProductsRequest;
import com.org.scraper_bkd.exception.PriceTrackerDBError;
import com.org.scraper_bkd.exception.PriceTrackerExistedAlready;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.ProductScraperRepo;
import com.org.scraper_bkd.service.PriceTrackerService;
import com.org.scraper_bkd.service.ProductScraperService;
import com.org.scraper_bkd.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PriceTrackerController {

    private static final Logger logger = LoggerFactory.getLogger(PriceTrackerController.class);

    private  final ProductScraperRepo productScraperRepo;
    private final PriceTrackerService priceTrackerService;
    private final ProductScraperService productScraperService;


    @PostMapping("/priceTrackerRequest")
    public ResponseEntity<?> saveTrackerRequest(@Valid @RequestBody PriceTrackerRequest priceTrackerRequest) {
        try {

            priceTrackerRequest.setEmail(AuthUtils.email());
            priceTrackerRequest.setPhoneNumber(AuthUtils.phoneNumber());

                Optional<ProductScraperModel> productScraperModel = productScraperRepo.findById(priceTrackerRequest.getProductId());

                if (productScraperModel.isPresent()) {
                    priceTrackerRequest.setProductScraperModel(productScraperModel.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Product not found with ID " + priceTrackerRequest.getProductId());
                }

            DashboardProductsDTO dashboardProductsDTO= priceTrackerService.savePriceTrackerRequest(priceTrackerRequest);
            return ResponseEntity.ok(dashboardProductsDTO);
        }
        catch(PriceTrackerExistedAlready | PriceTrackerDBError e){
            throw e;
        }
        catch (Exception e) {
            logger.error("Error: Failed to add price tracker : {}" ,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to add the price tracker");
        }
    }

    @PostMapping("/fetchUserProducts")
    public ResponseEntity<?> findUserProducts(@Valid @RequestBody UserProductsRequest userProductsRequest){
        try{
            System.out.println("fetchUserProducts api hit, user details ");
            System.out.println("email before :"+userProductsRequest.getEmail());
            System.out.println("number before :"+userProductsRequest.getPhoneNumber());


                userProductsRequest.setEmail(AuthUtils.email());
                userProductsRequest.setPhoneNumber(AuthUtils.phoneNumber());

            System.out.println("email after :"+userProductsRequest.getEmail());
            System.out.println("number after :"+userProductsRequest.getPhoneNumber());

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userId = auth.getName();
                String phoneNumber = null;

                // Use this approach to extract JWT claims
                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    phoneNumber = jwtAuth.getToken().getClaim("phoneNumber");
                }
                else{
                    System.out.println("auth class : "+auth.getClass().getName());
                }

                logger.info("authenticated user userId: {}", userId);
                logger.info("authenticated user phoneNumber: {}", phoneNumber);


                if(userProductsRequest.getEmail()==null ||  userProductsRequest.getPhoneNumber()==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing required Body");
            }

            List<DashboardProductsDTO> productList=priceTrackerService.findUserProducts(userProductsRequest);
            return ResponseEntity.ok(productList);
        }
        catch(Exception e){
            logger.error("Error at /fetchUserProducts : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch data");
        }
    }

    @GetMapping("/fetchPriceHistory")
    public ResponseEntity<?> fetchProductPriceChart(@RequestParam long id){
        try{
            Map<String,Object> priceChart=productScraperService.fetchPriceDataChart(id);
            if(priceChart!=null){
                return ResponseEntity.ok(priceChart);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unable to fetch Data.");
        }
        catch(Exception e){
            logger.error("Unexpected error at fetchProductPriceChart , {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch Data");
        }
    }

    @GetMapping("/fetchUserConfig")
    public ResponseEntity<?> fetchUserConfig(@RequestParam long id){
        try{
            PriceTrackerUsers trackerUsers=priceTrackerService.fetchUserNotificationConfig(id);
            if(trackerUsers==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User config not found");
            }
            System.out.println(trackerUsers);

            return ResponseEntity.ok(trackerUsers);
        }
        catch(Exception e){
            logger.error("Error at /fetchUserConfig : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to find user config");
        }
    }

    @PostMapping("/updateUserConfig")
    public ResponseEntity<?> updateUserConfig(@RequestBody  PriceTrackerUsers priceTrackerUsers){
        try{

            priceTrackerUsers.setEmail(AuthUtils.email());
            priceTrackerUsers.setPhoneNumber(AuthUtils.phoneNumber());

            PriceTrackerUsers updatedData=priceTrackerService.updateUserNotificationConfig(priceTrackerUsers);
            if(updatedData==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to update the config");
            }
            return ResponseEntity.ok(updatedData);
        }
        catch(Exception e){
            logger.error("Error at /updateUserConfig : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update the config");
        }
    }

    @PostMapping("/deleteTracker")
    public ResponseEntity<?> deleteProductTracker(@RequestBody Map<String,Long> trackingId) {
        try {
            if (trackingId.isEmpty() || trackingId.get("id") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid Request");
            }

            priceTrackerService.deleteTracker(trackingId.get("id"));
            return ResponseEntity.ok("Tracker deleted");


        } catch (Exception e) {
            logger.error("Unexpected error at deleteProductTracker : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

        @PostMapping("/fetchExistedProductTracker")
        public ResponseEntity<?> fetchExistedProductTracker(@RequestBody UserProductsRequest userRequest){
            try{

                userRequest.setEmail(AuthUtils.email());
                userRequest.setPhoneNumber(AuthUtils.phoneNumber());

                String email=userRequest.getEmail();
                String phone=userRequest.getPhoneNumber();
                long product_id= userRequest.getProduct_id();
                if(email == null || phone == null || product_id==0){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Bad request");
                }
                PriceTrackerUsers priceTrackerUser =priceTrackerService.fetchUserConfigFromProductID(userRequest);
                if(priceTrackerUser==null){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Tracker not found");
                }
                return ResponseEntity.ok(priceTrackerUser);
            }
            catch(Exception e){
                logger.error("Error at fetchExistedProductTracker : {}",e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unable to fetch user config from product id ");
            }

        }


}
