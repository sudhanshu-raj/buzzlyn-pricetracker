package com.org.scraper_bkd.service;

/**
 * This class is responsible for fetching the product details ,
 * like when user puts the url on input field, we extract product details from here,
 * saves the new product, updates the existing product etc.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.org.scraper_bkd.config.AppConfig;
import com.org.scraper_bkd.dto.PriceScraperDTO;
import com.org.scraper_bkd.dto.ProductScraperDTO;
import com.org.scraper_bkd.exception.BadClient_Request;
import com.org.scraper_bkd.exception.InvalidPythonAPI_Request;
import com.org.scraper_bkd.model.PriceHistory;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.PriceHistoryRepo;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import com.org.scraper_bkd.repo.ProductScraperRepo;
import com.org.scraper_bkd.utils.ProductHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.org.scraper_bkd.constants.AppConstant.*;

@Service
@RequiredArgsConstructor
public class ProductScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ProductScraperService.class);

    private  final RestTemplate restTemplate;
    private  final AppConfig appConfig;
    private  final ProductScraperRepo productScraperRepo;
    private  final OllamaService ollamaService;
    private final ProductHelper productHelper;
    private final PriceTrackerRepo priceTrackerRepo;
    private final PriceHistoryRepo priceHistoryRepo;


    public ProductScraperModel getProductDetails(String url) {
        try {
            ProductScraperDTO productScraperDTO = fetchProductDetailsAPI(url);
          //  ProductScraperModel existedProductData = searchProductData_byNameBrand(productScraperDTO.getProductName(), productScraperDTO.getBrand());
            ProductScraperModel existedProductData=searchProductData_byUrlIdentifier(extractUrlIdentifier(url));
            if (existedProductData != null) {
                long existedProductID = existedProductData.getId();
                long existedPrice = existedProductData.getPrice();

                if (productScraperDTO.getPrice() != 0 && existedPrice != productScraperDTO.getPrice()) {
                    productScraperRepo.updatePrice(productScraperDTO.getPrice(), existedProductData.getId());
                }
                long mrp = existedProductData.getMrp();
                if (productScraperDTO.getMrp() != 0 && mrp != productScraperDTO.getMrp()) {
                    productScraperRepo.updateMRP(productScraperDTO.getMrp(), existedProductData.getId());
                }

                String stock = existedProductData.getStock_status();
                if (productScraperDTO.getStock_status() != null && !stock.equalsIgnoreCase(productScraperDTO.getStock_status())) {
                    productScraperRepo.updateStockStatus(productScraperDTO.getStock_status(), existedProductData.getId());
                }
                if (productScraperDTO.getRatings() != 0 && existedProductData.getRatings() != productScraperDTO.getRatings()) {
                    productScraperRepo.updateRatings(productScraperDTO.getRatings(), existedProductID);
                }
                if (productScraperDTO.getReviews() != 0 && existedProductData.getReviews() != productScraperDTO.getReviews()) {
                    productScraperRepo.updateReviews(productScraperDTO.getReviews(), existedProductID);
                }
                System.out.println("returning existed product data");
                return searchProductData_byID(existedProductID);
            } else {
                ProductScraperModel productScraperModel = ProductScraperModel.builder()
                        .brand(productScraperDTO.getBrand())
                        .productName(productScraperDTO.getProductName())
                        .price(productScraperDTO.getPrice())
                        .mrp(productScraperDTO.getMrp())
                        .currency(productScraperDTO.getCurrency())
                        .reviews(productScraperDTO.getReviews())
                        .ratings(productScraperDTO.getRatings())
                        .productURL(productScraperDTO.getProductURL())
                        .imageURL(productScraperDTO.getImageURL())
                        .stock_status(productScraperDTO.getStock_status())
                        .specs(productScraperDTO.getSpecs())
                        .urlIdentifier(extractUrlIdentifier(url))
                        .tracking_status(false)
                        .technicalDetails(productScraperDTO.getTechnicalDetails())
                        .build();

                saveProductData(productScraperModel);
                return productScraperModel;
            }
        }
        catch(BadClient_Request | InvalidPythonAPI_Request e){
            throw e;
        }
        catch (Exception e){
            logger.error("Error while scraping the product : {}",e.getMessage());
            return null;
        }

    }

    public ProductScraperDTO fetchProductDetailsAPI(String url) {
        String python_scraper_api_url = appConfig.getScraperUrl() + "/scrape";

        HttpHeaders header = new HttpHeaders();
        header.set("Scraper-API", appConfig.getScraperApiKey());
        header.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", url);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, header);

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(
                    python_scraper_api_url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                String errorMsg = "Status: " + response.getStatusCode() + ", message: " + response.getBody();
                logger.error("Error while sending the API request to Python scraper, Error:: {}", errorMsg);
                String responseBody = response.getBody() != null ? response.getBody().toString() : "No response body";
                throw new InvalidPythonAPI_Request(
                        "Error while sending the API request to Python scraper",
                        HttpStatus.valueOf(response.getStatusCode().value()),
                        responseBody
                );
            }
            JsonNode responseBody = response.getBody();

            String status = responseBody.get("status").asText();
            if (status.equals("success")) {
                JsonNode data = responseBody.get("data");
                String error=data.has("error") ? data.get("error").asText() : null;
                if (error==null){
                boolean valid_product_page=data.has("valid_product_page") ? data.get("valid_product_page").asBoolean() : false;
                boolean physical_product=data.has("physical_product") ? data.get("physical_product").asBoolean() : false;
                    System.out.println(data);
                if (valid_product_page && physical_product) {
                    String brand = data.has("brand")? data.get("brand").asText() : null;
                    String productURL = url;
                    String title = data.has("title") ? data.get("title").asText() : null;
                    long price = data.has("price") ? data.get("price").asLong() : 0;
                    long mrp = data.has("mrp") ? data.get("mrp").asLong() : 0;
                    String currency=data.has("currency") ? data.get("currency").asText() : null;
                    System.out.println("currency extracted is " + currency);
                    String availability_str = data.has("stock") ? data.get("stock").asText() : null;
                    if (!availability_str.equalsIgnoreCase("in_stock") && !availability_str.equalsIgnoreCase("out_stock")) {
                        int availability = ollamaService.checkProductAvailability(availability_str).block();
                        if (availability == 0) {
                            availability_str = "out_stock";
                        } else {
                            availability_str = "in_stock";
                        }
                    }
                    float ratings = data.has("ratings") ? (float) data.get("ratings").asDouble() : 0;
                    int reviews = data.has("reviews") ? (int) data.get("reviews").asLong() : 0;
                    String imageURL = "";
                    if (data.has("imageURL")) {
                        imageURL = data.get("imageURL").asText();
                    } else if (data.has("image")) {
                        imageURL = data.get("image").asText();
                    }


                    JsonNode specs = data.has("specs") ? data.get("specs") : null;
                    JsonNode technical_details = data.has("technical_details") ? data.get("technical_details") : null;


                    if (!(price <= 0 && availability_str.equalsIgnoreCase("in_stock"))) {
                        ProductScraperDTO productScraperDTO = ProductScraperDTO.builder()
                                .brand(brand)
                                .productName(title)
                                .price(price)
                                .mrp(mrp)
                                .currency(currency)
                                .ratings(ratings)
                                .reviews(reviews)
                                .imageURL(imageURL)
                                .specs(specs)
                                .technicalDetails(technical_details)
                                .productURL(productURL)
                                .stock_status(availability_str)
                                .build();
                        return productScraperDTO;
                    } else {
                        String responseBody2 = response.getBody() != null ? response.getBody().toString() : "No response body";
                        throw new InvalidPythonAPI_Request(
                                "Seems Unable to scrape the product,as we not able to fetch the price",
                                HttpStatus.valueOf(response.getStatusCode().value()),
                                responseBody2
                        );
                    }
                }
                else{
                    String responseBody2 = response.getBody() != null ? response.getBody().toString() : "No response body";
                    throw new BadClient_Request(
                            "Seems Product is not a valid_product_page or physical_product",
                            HttpStatus.BAD_REQUEST,
                            responseBody2
                    );
                }

            }
            else{
                    String responseBody2 = response.getBody() != null ? response.getBody().toString() : "No response body";
                    throw new InvalidPythonAPI_Request(
                            "Seems we are unable to fetch the details, using gemini scrapers",
                            HttpStatus.valueOf(response.getStatusCode().value()),
                            responseBody2
                    );
                }
            }
            else {
                String responseBody2 = response.getBody() != null ? response.getBody().toString() : "No response body";
                throw new InvalidPythonAPI_Request(
                        "Unable to get the status of Success from PYTHON API",
                        HttpStatus.valueOf(response.getStatusCode().value()),
                        responseBody2
                );
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new InvalidPythonAPI_Request("HTTP error occurred while calling Python API", (HttpStatus) e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new InvalidPythonAPI_Request("Error while sending the API request to Python scraper", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        catch(InvalidPythonAPI_Request | BadClient_Request e){
            throw e;
        }
        catch (Exception e) {
            throw new InvalidPythonAPI_Request("Unexpected error while sending the API request to Python scraper", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    public void saveProductData(ProductScraperModel productScraperModel) {
        productScraperRepo.save(productScraperModel);
    }
    public ProductScraperModel searchProductData_byNameBrand(String productName,String brand) {
        List<ProductScraperModel> result=productScraperRepo.findByNameAndBrand(productName, brand);
        return result.isEmpty() ? null : result.get(0);
    }
    public ProductScraperModel searchProductData_byUrlIdentifier(String urlIdentifier){
        List<ProductScraperModel> productScraperModels= productScraperRepo.findByUrlIdentifier(urlIdentifier);

        if (productScraperModels.isEmpty()) {
            logger.info("No data found by url identifier: {}", urlIdentifier);
            return null;
        }
        return productScraperModels.get(0);

    }

    public ProductScraperModel searchProductData_byID(long productID) {
        Optional<ProductScraperModel> product= productScraperRepo.findById(productID);
        return product.orElse(null);
    }

    public void updateProductData(PriceScraperDTO newProductData,long productId,boolean nonTrackedProducts){

        try {
            ProductScraperModel existedProduct=productScraperRepo.findById(productId).orElseThrow(
                    ()->  new  RuntimeException("Unable to find the product id by  " +productId)
            );
            boolean isProductUpdated=false;
            if (newProductData.getPrice() != 0 && newProductData.getPrice()!=existedProduct.getPrice()){
                existedProduct.setPrice(newProductData.getPrice());
                isProductUpdated=true;
            }
            if(newProductData.getMrp()!=0 && newProductData.getMrp()!= existedProduct.getMrp()){
                existedProduct.setMrp(newProductData.getMrp());
                isProductUpdated=true;
            }
            if(newProductData.getStock()!=null && !newProductData.getStock().equalsIgnoreCase(existedProduct.getStock_status())){
                existedProduct.setStock_status(newProductData.getStock());
                isProductUpdated=true;
            }
            if(newProductData.getReviews()!=0 && newProductData.getReviews()!=existedProduct.getReviews()){
                existedProduct.setReviews(newProductData.getReviews());
                isProductUpdated=true;
            }
            if(newProductData.getRatings()!=0 && newProductData.getRatings()!=existedProduct.getRatings()){
                existedProduct.setRatings(newProductData.getRatings());
                isProductUpdated=true;
            }
            if(newProductData.isTracking_status()!=existedProduct.isTracking_status()){
                existedProduct.setTracking_status(newProductData.isTracking_status());
                isProductUpdated=true;
            }
            existedProduct.setUpdatedOn(LocalDateTime.now());
            if(isProductUpdated || nonTrackedProducts){
                productScraperRepo.save(existedProduct);
                logger.info("Existed product id {} , updated with new data ",productId);
            }
        }
        catch(Exception e){
            logger.error("Erroe while updating the product data in scraper table : {}",e.getMessage());
        }
    }

    public String extractUrlIdentifier(String url){
        try{
            String domain= productHelper.extractBrand(url);
            if(AMAZON_IND.equalsIgnoreCase(domain)){
                return productHelper.getAmazonProductIdentifier(url);
            }
            else if(FLIPKART.equalsIgnoreCase(domain)){
                return productHelper.getFlipkartProductIdentifier(url);
            }
            return url;
        }
        catch(Exception e){
            logger.error("Error at extractUrlIdentifier : {}",e.getMessage());
        }
        return url;
    }

    public Map<String, Object> fetchPriceDataChart(long id) {
        try{
            PriceTrackerModel priceTrackerModel=priceTrackerRepo.findById(id).orElse(null);
            if(priceTrackerModel!=null){
                List<PriceHistory> priceHistoryList=priceHistoryRepo.findByProductScraperModelId(
                        priceTrackerModel.getProductScraperModel().getId());

                List<Map<String, Object>> priceHistoryData = new ArrayList<>();

                for (PriceHistory history : priceHistoryList) {
                    Map<String, Object> entry = new HashMap<>();
                    // Format LocalDateTime to YYYY-MM-DD
                    String formattedDate = history.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    entry.put("date", formattedDate);
                    String currency= ProductHelper.getCurrencySymbol(priceTrackerModel.getProductScraperModel().getCurrency());
                    entry.put("price", currency+history.getPrice());
                    priceHistoryData.add(entry);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("priceHistory", priceHistoryData);
                return result;
            }
            return null;
        }
        catch(Exception e){
            logger.error("Error at fetchPriceDataChart , {}",e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        ProductScraperService scraperService = new ProductScraperService(null,null,null,null,null,null,null);
        String url="https://www.amazon.in/Majestic-Man-Classic-Cotton-Casual/dp/B0CK6LC8QR/ref=sr_1_7?sr=8-7";
        System.out.println(scraperService.extractUrlIdentifier(url));

    }


}
