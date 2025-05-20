package com.org.scraper_bkd.service;

/**
 * This class is reponsible for managing the price tracker request,
 * it saved new request, fetched product price details using python api
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.org.scraper_bkd.config.AppConfig;
import com.org.scraper_bkd.dto.*;
import com.org.scraper_bkd.enums.NotificationFrequency;
import com.org.scraper_bkd.enums.TrackingStatus;
import com.org.scraper_bkd.exception.BadClient_Request;
import com.org.scraper_bkd.exception.InvalidPythonAPI_Request;
import com.org.scraper_bkd.exception.PriceTrackerDBError;
import com.org.scraper_bkd.exception.PriceTrackerExistedAlready;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import com.org.scraper_bkd.repo.PriceTrackerUserRepo;
import com.org.scraper_bkd.repo.ProductScraperRepo;
import com.org.scraper_bkd.utils.ProductHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.org.scraper_bkd.constants.AppConstant.PINCODE_TRACKER_ENDPOINT;
import static com.org.scraper_bkd.constants.AppConstant.PRICE_SCRAPER_ENDPOINT;

@Service
@RequiredArgsConstructor
public class PriceTrackerService {

    private static final Logger logger = LoggerFactory.getLogger(ProductScraperService.class);

    private  final AppConfig appConfig;
    private  final RestTemplate restTemplate;
    private  final OllamaService ollamaService;
    private final PriceTrackerRepo priceTrackerRepo;
    private final ProductScraperService productScraperService;
    private final PriceTrackerUserRepo priceTrackerUserRepo;
    private final PincodeTrackerDTO pincodeTrackerDTO;
    private final ProductScraperRepo productScraperRepo;
    private final ProductHelper helper;


    @Transactional
    public DashboardProductsDTO savePriceTrackerRequest(PriceTrackerRequest priceTrackerRequest) {
        // Input validation
        if (priceTrackerRequest == null || priceTrackerRequest.getProductId() == null) {
            throw new BadClient_Request("Invalid price tracker request", HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        try {
            boolean isTrackerExists = checkExistedTracker(priceTrackerRequest);
            DashboardProductsDTO dashboardProductsDTO;

            if (!isTrackerExists) {
                dashboardProductsDTO = savePriceTracker_Details(priceTrackerRequest);
            } else {
                dashboardProductsDTO = updateUserExistingTracker(priceTrackerRequest);
            }

            if (dashboardProductsDTO == null) {
                throw new PriceTrackerDBError("Unable to save the request", HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error while saving the price tracker request");
            }

            logger.info("Successfully processed price tracker for product ID: {}", priceTrackerRequest.getProductId());
            return dashboardProductsDTO;
        }
        catch (PriceTrackerExistedAlready | PriceTrackerDBError e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error at savePriceTrackerRequest: {}", e.getMessage());
            throw new PriceTrackerDBError("Failed to save price tracker request",
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public PincodeTrackerDTO fetchPincodeTracker(String url,String pincode) {

        //check if the url even supports pincode tracking or not
        if(!helper.isPincodeTrackingSupports(url)){
            return null;
        }

        PincodeTrackerDTO result = new PincodeTrackerDTO();
        String python_pincodeTracker_api = appConfig.getScraperUrl() + PINCODE_TRACKER_ENDPOINT;

        HttpHeaders header = new HttpHeaders();
        header.set("Scraper-API", appConfig.getScraperApiKey());
        header.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", url);
        requestBody.put("pincode",pincode);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, header);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    python_pincodeTracker_api,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                String errorMsg = "Status: " + response.getStatusCode() + ", message: " + response.getBody();
                logger.error("Error while sending the API request to Python scraper, error:: {}", errorMsg);
                String responseBody = response.getBody() != null ? response.getBody().toString() : "No response body";
                throw new InvalidPythonAPI_Request(
                        "Error while sending the API request to scraper",
                        HttpStatus.valueOf(response.getStatusCode().value()),
                        responseBody
                );
            }

            JsonNode responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                throw new InvalidPythonAPI_Request(
                        "Something went wrong, didn't get response from scraper endpoint: " + PINCODE_TRACKER_ENDPOINT,
                        HttpStatus.BAD_REQUEST,
                        "Response is null"
                );
            }

            String status = responseBody.get("status").asText();
            if (status.equals("success")) {
                JsonNode data = responseBody.get("data");
                if (data != null) {
                    String error = responseBody.has("error") ? responseBody.get("error").asText() : null;
                    if (error != null) {
                        logger.error("Got error in response body for scraper api {} error: {}",
                                PINCODE_TRACKER_ENDPOINT, error);
                        throw new BadClient_Request(
                                "Error from scraper API: " + error,
                                HttpStatus.BAD_REQUEST,
                                error);
                    }

                    String deliveryDate = data.has("delivery_date") ? data.get("delivery_date").asText() : null;
                    if (deliveryDate != null && !deliveryDate.equalsIgnoreCase("N/A")) {
                        result.setStockAvailable(true);
                        result.setDeliveryDate(deliveryDate);
                    } else {
                        result.setStockAvailable(false);
                        result.setDeliveryDate(null);
                    }
                }
            } else {
                logger.error("Didn't get success response from scraper api: {} error: {}",
                        PINCODE_TRACKER_ENDPOINT, responseBody);
                throw new BadClient_Request(
                        "Failed to get successful response from scraper API",
                        HttpStatus.BAD_REQUEST,
                        responseBody.toString());
            }

            return result;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new InvalidPythonAPI_Request("HTTP error from Python API",
                    HttpStatus.BAD_GATEWAY, e.getResponseBodyAsString());
        } catch (InvalidPythonAPI_Request | BadClient_Request e) {
            throw e; // Rethrow these exceptions
        } catch (Exception e) {
            logger.error("Unexpected error at fetchPincodeTracker, error: {}", e.getMessage());
            throw new BadClient_Request("Unexpected error processing pincode tracking",
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

//    public PriceScraperDTO fetchPriceScraper_API(String url){
//        String python_scraper_api_url = appConfig.getScraperUrl() + PRICE_SCRAPER_ENDPOINT;
//
//        HttpHeaders header = new HttpHeaders();
//        header.set("Scraper-API", appConfig.getApiKey());
//        header.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, String> requestBody = new HashMap<>();
//        requestBody.put("url", url);
//
//        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, header);
//        ResponseEntity<JsonNode> response;
//        try {
//            response = restTemplate.exchange(
//                    python_scraper_api_url,
//                    HttpMethod.POST,
//                    requestEntity,
//                    JsonNode.class
//            );
//
//            if (response.getStatusCode() != HttpStatus.OK) {
//                String errorMsg = "Status: " + response.getStatusCode() + ", message: " + response.getBody();
//                logger.error("Error while sending the API request to Python scraper, Error:: {}", errorMsg);
//                String responseBody = response.getBody() != null ? response.getBody().toString() : "No response body";
//                throw new InvalidPythonAPI_Request(
//                        "Error while sending the API request to Python scraper",
//                        HttpStatus.valueOf(response.getStatusCode().value()),
//                        responseBody
//                );
//            }
//            JsonNode responseBody = response.getBody();
//            String status=null;
//            if (responseBody != null && !responseBody.isEmpty()) {
//                status = responseBody.get("status").asText();
//            }
//
//            if (status!=null && status.equals("success")) {
//                JsonNode data = responseBody.get("data");
//                String error = data.has("error") ? data.get("error").asText() : null;
//                if (error == null) {
//                    String title=data.has("title") ? data.get("title").asText() : null;
//                    long price=data.has("price") ? data.get("price").asLong() : 0;
//                    long mrp=data.has("mrp") ? data.get("mrp").asLong() : 0;
//                    String availability_str = data.has("stock") ? data.get("stock").asText() : null;
//                    if ((!availability_str.equalsIgnoreCase("in_stock") && !availability_str.equalsIgnoreCase("out_stock"))) {
//                        int availability = ollamaService.checkProductAvailability(availability_str).block();
//                        if (availability == 0) {
//                            availability_str = "out_stock";
//                        } else {
//                            availability_str = "in_stock";
//                        }
//                    }
//                    float ratings = data.has("ratings") ? (float) data.get("ratings").asDouble() : 0;
//                    int reviews = data.has("reviews") ? (int) data.get("reviews").asLong() : 0;
//                    String brand = data.has("brand")? data.get("brand").asText() : null;
//                    if ( (price <= 0 && availability_str.equalsIgnoreCase("in_stock"))) {
//                        logger.warn("Seems , we are not able to fetch the price using gemini Image scraper");
//                        logger.info("Trying again, with gemini html scraper ...");
//                        ProductScraperDTO productScraperDTO = productScraperService.fetchProductDetailsAPI(url);
//                        if (productScraperDTO != null) {
//                            return PriceScraperDTO.builder()
//                                    .title(productScraperDTO.getProductName())
//                                    .price(productScraperDTO.getPrice())
//                                    .mrp(productScraperDTO.getMrp())
//                                    .stock(productScraperDTO.getStock_status())
//                                    .reviews(productScraperDTO.getReviews())
//                                    .ratings(productScraperDTO.getRatings())
//                                    .brand(productScraperDTO.getBrand())
//                                    .build();
//                        }
//                    }
//
//                    if (!(price <= 0 && availability_str.equalsIgnoreCase("in_stock"))){
//
//                        return PriceScraperDTO.builder()
//                                .title(title)
//                                .price(price)
//                                .mrp(mrp)
//                                .stock(availability_str)
//                                .reviews(reviews)
//                                .ratings(ratings)
//                                .brand(brand)
//                                . build();
//
//                    }
//                    else{
//                        String responseBody2 = response.getBody() != null ? response.getBody().toString() : "No response body";
//                        throw new InvalidPythonAPI_Request(
//                                "Seems Unable to scrape the product,as we not able to fetch the price",
//                                HttpStatus.valueOf(response.getStatusCode().value()),
//                                responseBody2
//                        );
//                    }
//
//
//                } else {
//                    throw new InvalidPythonAPI_Request(
//                            "Error while scraping product price",
//                            HttpStatus.valueOf(response.getStatusCode().value()),
//                            error
//                    );
//                }
//            }
//                else {
//                    String responseBody2 = response.getBody() != null ? response.getBody().toString() : "No response body";
//                    throw new InvalidPythonAPI_Request(
//                            "Unable to get the status of Success from PYTHON Price Scraper API",
//                            HttpStatus.valueOf(response.getStatusCode().value()),
//                            responseBody2
//                    );
//                }
//
//
//        }
//        catch (HttpClientErrorException | HttpServerErrorException e) {
//            throw new InvalidPythonAPI_Request("HTTP error occurred while calling Python API", (HttpStatus) e.getStatusCode(), e.getResponseBodyAsString());
//        } catch (RestClientException e) {
//            throw new InvalidPythonAPI_Request("Error while sending the API request to Python scraper", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
//        }
//        catch(InvalidPythonAPI_Request | BadClient_Request e){
//            throw e;
//        }
//        catch (Exception e) {
//            throw new InvalidPythonAPI_Request("Unexpected error while sending the API request to Python scraper", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
//        }
//    }

    public PriceScraperDTO fetchPriceScraper_API(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new BadClient_Request("Product URL cannot be empty", HttpStatus.BAD_REQUEST, "Empty URL");
        }

        try {
            // Try primary scraper first
            PriceScraperDTO result = fetchFromPrimaryScraper(url);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            logger.warn("Primary scraper failed: {}", e.getMessage());
            // Continue to backup scraper
        }

        // If we get here, primary scraper failed - try backup
        try {
            logger.info("Trying backup scraper for URL: {}", url);
            return fetchFromBackupScraper(url);
        } catch (Exception e) {
            logger.error("Both scrapers failed. Error from backup scraper: {}", e.getMessage());
            throw new InvalidPythonAPI_Request(
                    "All scrapers failed to extract product data",
                    HttpStatus.BAD_GATEWAY,
                    e.getMessage());
        }
    }

    private PriceScraperDTO fetchFromPrimaryScraper(String url) {
        String python_scraper_api_url = appConfig.getScraperUrl() + PRICE_SCRAPER_ENDPOINT;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Scraper-API", appConfig.getScraperApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", url);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                python_scraper_api_url,
                HttpMethod.POST,
                requestEntity,
                JsonNode.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        JsonNode responseBody = response.getBody();
        if (responseBody == null || responseBody.isEmpty()) {
            return null;
        }

        // Check status field
        JsonNode statusNode = responseBody.get("status");
        if (statusNode == null || !statusNode.asText().equals("success")) {
            return null;
        }

        // Process data
        JsonNode data = responseBody.get("data");
        if (data == null || (data.has("error") && !data.get("error").isNull())) {
            return null;
        }

        // Extract product data
        String title = data.has("title") ? data.get("title").asText() : null;
        long price = data.has("price") ? data.get("price").asLong() : 0;
        long mrp = data.has("mrp") ? data.get("mrp").asLong() : 0;
        String availability_str = data.has("stock") ? data.get("stock").asText() : "unknown";
        float ratings = data.has("ratings") ? (float) data.get("ratings").asDouble() : 0;
        int reviews = data.has("reviews") ? data.get("reviews").asInt() : 0;
        String brand = data.has("brand") ? data.get("brand").asText() : null;

        // Process availability
        if (availability_str != null &&
                !availability_str.equalsIgnoreCase("in_stock") &&
                !availability_str.equalsIgnoreCase("out_stock")) {
            try {
                int availability = ollamaService.checkProductAvailability(availability_str).block();
                availability_str = (availability > 0) ? "in_stock" : "out_stock";
            } catch (Exception e) {
                logger.warn("Failed to determine availability: {}", e.getMessage());
                availability_str = "unknown";
            }
        }

        // Return null for in-stock items without price (will trigger backup)
        if (price <= 0 && "in_stock".equalsIgnoreCase(availability_str)) {
            return null;
        }

        return PriceScraperDTO.builder()
                .title(title)
                .price(price)
                .mrp(mrp)
                .stock(availability_str)
                .reviews(reviews)
                .ratings(ratings)
                .brand(brand)
                .build();
    }

    private PriceScraperDTO fetchFromBackupScraper(String url) {
        ProductScraperDTO backupResult = productScraperService.fetchProductDetailsAPI(url);
        if (backupResult == null) {
            throw new InvalidPythonAPI_Request(
                    "Backup scraper returned null result",
                    HttpStatus.BAD_GATEWAY,
                    "No data available from backup scraper"
            );
        }

        // Even if price is 0, return data anyway - better than nothing
        return PriceScraperDTO.builder()
                .title(backupResult.getProductName())
                .price(backupResult.getPrice())
                .mrp(backupResult.getMrp())
                .stock(backupResult.getStock_status())
                .reviews(backupResult.getReviews())
                .ratings(backupResult.getRatings())
                .brand(backupResult.getBrand())
                .build();
    }

    public boolean checkExistedTracker(PriceTrackerRequest priceTrackerRequest){
        try {
            PriceTrackerModel priceTrackerModel=priceTrackerRepo.findExistingTracker(
                    priceTrackerRequest.getProductId(),
                    priceTrackerRequest.getEmail(),
                    priceTrackerRequest.getPhoneNumber());

            return priceTrackerModel != null;
        }
        catch (Exception e) {
            logger.error("Error while checking existing tracker data : {}",e.getMessage());
            return false;
        }
    }

    @Transactional
    public DashboardProductsDTO savePriceTracker_Details(PriceTrackerRequest priceTrackerRequest){

        try {
            LocalDateTime notificationUpdateDate=null;
            if(priceTrackerRequest.isNotificationFrequencySet() && priceTrackerRequest.getNotificationFrequencyValue().equals(NotificationFrequency.CUSTOM)){
                notificationUpdateDate=LocalDateTime.now();
            }
            //save user config
            PriceTrackerUsers priceTrackerUsers=PriceTrackerUsers.builder()
                    .email(priceTrackerRequest.getEmail())
                    .phoneNumber(priceTrackerRequest.getPhoneNumber())
                    .emailSMSEnabled(priceTrackerRequest.isEmailSMSEnabled())
                    .phoneSMSEnabled(priceTrackerRequest.isPhoneSMSEnabled())
                    .whatsappSMSEnabled(priceTrackerRequest.isWhatsappSMSEnabled())
                    .pushSMSEnabled(priceTrackerRequest.isPushSMSEnabled())
                    .automaticAlert(priceTrackerRequest.isAutomaticAlert())
                    .customPriceAlert(priceTrackerRequest.isCustomPriceAlert())
                    .stockAlert(priceTrackerRequest.isStockAlert())
                    .customPrice(priceTrackerRequest.getCustomPrice())
                    .pincodeStockTracking(priceTrackerRequest.isPincodeStockTracking())
                    .pincode(priceTrackerRequest.getPincode())
                    .notificationFrequencySet(priceTrackerRequest.isNotificationFrequencySet())
                    .notificationFrequencyValue(priceTrackerRequest.getNotificationFrequencyValue())
                    .customNotificationDays(priceTrackerRequest.getCustomNotificationDays())
                    .notificationUpdateDate(notificationUpdateDate)
                    .build();

            PriceTrackerUsers newUserRequest=priceTrackerUserRepo.save(priceTrackerUsers);

            //save the price tracker model , after getting user config id
            PriceTrackerModel priceTrackerModel = PriceTrackerModel.builder()
                    .productScraperModel(priceTrackerRequest.getProductScraperModel())
                    .priceTrackerUsers(newUserRequest)
                    .first_time_price(priceTrackerRequest.getProductScraperModel().getPrice())
                    .current_time_price(priceTrackerRequest.getProductScraperModel().getPrice())
                    .lastChecked(LocalDateTime.now())
                    .status(TrackingStatus.TRACKING)
                    .nextScrapeTime(LocalDateTime.now().plusHours(4))    // 4 hours
                    .build();
            PriceTrackerModel newData=priceTrackerRepo.save(priceTrackerModel);

            //update the product scraper model that this product is in tracking now
            ProductScraperModel productScraperModel=priceTrackerRequest.getProductScraperModel();
            productScraperModel.setTracking_status(true);
            productScraperRepo.save(productScraperModel);

            //Map the result to DashboardProductsDTO
            DashboardProductsDTO productsDTO=new DashboardProductsDTO(priceTrackerModel);

            logger.info("New price tracker request added of id : {}",newData.getId());
            return productsDTO;

        }
        catch (Exception e) {
            logger.error("Error while saving new tracker request : {}",e.getMessage());
            throw new RuntimeException("Unable to save new tracker");
        }
    }

    public List<DashboardProductsDTO> findUserProducts(UserProductsRequest request) {
        try {
            List<PriceTrackerModel> priceTrackerModels = priceTrackerRepo.findUserProducts(
                    request.getEmail(), request.getPhoneNumber());

            if (priceTrackerModels == null || priceTrackerModels.isEmpty()) {
                return new ArrayList<>();

            }

            return priceTrackerModels.stream()
                    .map(DashboardProductsDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while finding user tracker products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public PriceTrackerUsers fetchUserNotificationConfig(long id) {
        try {
            PriceTrackerModel trackerModel=priceTrackerRepo.findById(id).orElse(null);
            if(trackerModel==null){
                return null;
            }
            return trackerModel.getPriceTrackerUsers();
        } catch (Exception e) {
            logger.error("Error fetching user notification config: {}", e.getMessage());
            return null;
        }
    }

    public PriceTrackerUsers updateUserNotificationConfig(PriceTrackerUsers priceTrackerUser){
        try {
            PriceTrackerUsers existingTrackerUser=priceTrackerUserRepo.findById(priceTrackerUser.getId()).orElseThrow(
                    () -> new RuntimeException("Tracker user details not found ")
            );
            if( existingTrackerUser.getCustomNotificationDays()!=priceTrackerUser.getCustomNotificationDays()){
                priceTrackerUser.setNotificationUpdateDate(LocalDateTime.now());
            }
            else{
                priceTrackerUser.setNotificationUpdateDate(existingTrackerUser.getNotificationUpdateDate());
            }

            PriceTrackerModel priceTrackerModel=priceTrackerRepo.findByPriceTrackerUser_Id(priceTrackerUser.getId());
            priceTrackerModel.setStatus(TrackingStatus.TRACKING);
            priceTrackerRepo.save(priceTrackerModel);

            return priceTrackerUserRepo.save(priceTrackerUser);
        } catch (Exception e) {
            logger.error("Error updating config at updateUserNotificationConfig : {}", e.getMessage());
            return null;
        }
    }

    /*
    This is used to fetch user config from the product id, like when user fetch product details
    then we have to check wether user has this price tracker of this product or not , if yes
    then return this config and can overwrite it and if no then they can create one
     */
    public PriceTrackerUsers fetchUserConfigFromProductID(UserProductsRequest userProductsRequest){
        try{
            PriceTrackerModel priceTrackerModel=priceTrackerRepo.findUserConfigByProductId( userProductsRequest.getEmail() ,
                    userProductsRequest.getPhoneNumber(),
                    userProductsRequest.getProduct_id());
            if(priceTrackerModel!=null){
                return priceTrackerModel.getPriceTrackerUsers();
            }
            return null;
        }
        catch(Exception e){
            logger.error("Error at fetchUserConfigFromProductID : {}",e.getMessage());
            return null;
        }
    }

    @Transactional
    public DashboardProductsDTO updateUserExistingTracker(PriceTrackerRequest priceTrackerRequest){
        try{
            PriceTrackerUsers trackerUser=priceTrackerUserRepo.findById(priceTrackerRequest.getUserConfigId()).orElseThrow(
                    () -> new RuntimeException("User Config not found with ID: " + priceTrackerRequest.getUserConfigId())
            );
            trackerUser.setEmailSMSEnabled(priceTrackerRequest.isEmailSMSEnabled());
            trackerUser.setPhoneSMSEnabled(priceTrackerRequest.isPhoneSMSEnabled());
            trackerUser.setWhatsappSMSEnabled(priceTrackerRequest.isWhatsappSMSEnabled());
            trackerUser.setPushSMSEnabled(priceTrackerRequest.isPushSMSEnabled());
            trackerUser.setAutomaticAlert(priceTrackerRequest.isAutomaticAlert());
            trackerUser.setCustomPriceAlert(priceTrackerRequest.isCustomPriceAlert());
            trackerUser.setStockAlert(priceTrackerRequest.isStockAlert());
            trackerUser.setCustomPrice(priceTrackerRequest.getCustomPrice());
            trackerUser.setPincodeStockTracking(priceTrackerRequest.isPincodeStockTracking());
            trackerUser.setPincode(priceTrackerRequest.getPincode());
            trackerUser.setNotificationFrequencySet(priceTrackerRequest.isNotificationFrequencySet());
            trackerUser.setNotificationFrequencyValue(priceTrackerRequest.getNotificationFrequencyValue());
            trackerUser.setCustomNotificationDays(priceTrackerRequest.getCustomNotificationDays());
            if(trackerUser.getCustomNotificationDays()!=priceTrackerRequest.getCustomNotificationDays()){
                trackerUser.setNotificationUpdateDate(LocalDateTime.now());
            }

            priceTrackerUserRepo.save(trackerUser);

            // Update product tracking status
            ProductScraperModel productScraperModel = priceTrackerRequest.getProductScraperModel();
            if (productScraperModel == null) {
                throw new RuntimeException("Product model is missing in the request");
            }
            productScraperModel.setTracking_status(true);
            productScraperRepo.save(productScraperModel);

            PriceTrackerModel priceTrackerModel = priceTrackerRepo.findByUserConfigAndProductId(trackerUser.getId(),priceTrackerRequest.getProductId());
            if (priceTrackerModel == null) {
                throw new RuntimeException("No tracker found for this user config and product");
            }
            priceTrackerModel.setFirst_time_price(productScraperModel.getPrice());
            priceTrackerModel.setCurrent_time_price(productScraperModel.getPrice());
            priceTrackerModel.setLastChecked(LocalDateTime.now());
            priceTrackerModel.setNextScrapeTime(LocalDateTime.now().plusHours(4));  // 4 hours
            priceTrackerModel.setStatus(TrackingStatus.TRACKING);
            priceTrackerRepo.save(priceTrackerModel);

            //Map the result to DashboardProductsDTO
            DashboardProductsDTO productsDTO=new DashboardProductsDTO(priceTrackerModel);

            logger.info("Existing tracker request for id : {} , updated ",priceTrackerModel.getId());
            return productsDTO;
        }
        catch (Exception e){
            logger.error("Error at updateUserExistingTracker : {}",e.getMessage());
            throw new RuntimeException("Failed to update tracker: " + e.getMessage(), e);
        }
    }

    public void deleteTracker(Long id) {
        try{
            PriceTrackerModel trackerModel=priceTrackerRepo.findById(id).orElseThrow(
                    () ->   new RuntimeException("Tracking id not found")
            );
            ProductScraperModel scraperModel=trackerModel.getProductScraperModel();
            scraperModel.setTracking_status(false);
            productScraperRepo.save(scraperModel);

            long userId=trackerModel.getPriceTrackerUsers().getId();
            priceTrackerRepo.delete(trackerModel);
            priceTrackerUserRepo.deleteById(userId);


            logger.info("Tracker of id {} deleted successfully",id);

        }
        catch(Exception e){
            logger.error("Error deleting tracker with id {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
