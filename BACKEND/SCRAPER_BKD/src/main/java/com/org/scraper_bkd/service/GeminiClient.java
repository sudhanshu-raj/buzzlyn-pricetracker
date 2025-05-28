package com.org.scraper_bkd.service;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.org.scraper_bkd.constants.AppConstant.GEMINI_API_KEY;

@Component
public class GeminiClient {
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);

    Client client = Client.builder().apiKey(GEMINI_API_KEY).build();

    public int checkProductAvailability(String stock){
        try{

            String prompt = "Given the following stock availability status, respond with 1 if the item is in stock, and 0 if it is out of stock or unavailable. Only respond with 1 or 0.\n\n" +
                    "Examples:\n" +
                    "Input: \"Item is available for immediate purchase.\"\nOutput: 1\n" +
                    "Input: \"Out of stock. Expected restock in 2 weeks.\"\nOutput: 0\n" +
                    "Input: \"Limited availability.\"\nOutput: 1\n" +
                    "Input: \"Currently not in stock.\"\nOutput: 0\n" +
                    "Input: \"" + stock + "\"\nOutput:";

            GenerateContentResponse response =client
                    .models.generateContent("gemini-2.0-flash-001",prompt,null);

            String trimmedResponse = response.text().trim();
            if ("1".equals(trimmedResponse)) {
                return 1;
            } else if ("0".equals(trimmedResponse)) {
                return 0;
            } else {
                logger.warn("Gemini returned an unexpected output: '{}'", response.text());
                return 0; // Indicate an uncertain or erroneous result
            }
        }
        catch(Exception e){
            logger.error("Error at checkProductAvailability : {}",e.getMessage());
            return 0;
        }
    }

    public static void main(String[] args) {
        // Instantiate the client. The client by default uses the Gemini API. It
        //  gets the API key from the environment variable `GOOGLE_API_KEY`.
         GeminiClient obj=new GeminiClient();
        System.out.println("Stock status for \"In stock\": " + obj.checkProductAvailability("In stock"));
        System.out.println("Stock status for \"Currently unavailable\": " + obj.checkProductAvailability("Currently unavailable"));
        System.out.println("Stock status for \"Only 5 left!\": " + obj.checkProductAvailability("Only 5 left!"));
        System.out.println("Stock status for \"Backordered\": " + obj.checkProductAvailability("Backordered"));
        System.out.println("Stock status for \"Ships in 3-5 business days\": " + obj.checkProductAvailability("Ships in 3-5 business days"));
        System.out.println("Stock status for \"Not available \": "+obj.checkProductAvailability("Not available") );


    }


}