package com.org.scraper_bkd.service.notifications;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import static com.org.scraper_bkd.constants.AppConstant.TWILIO_ACCOUNT_SID;
import static com.org.scraper_bkd.constants.AppConstant.TWILIO_AUTH_TOKEN;

@Component
public class TwilioSMS {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSMS.class);

    public static final String ACCOUNT_SID =TWILIO_ACCOUNT_SID;
    public static final String AUTH_TOKEN = TWILIO_AUTH_TOKEN;

    public void sendPriceAlertSMS(String to , String productName,String brand,String old_price,String new_price,String product_url){
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            to = "whatsapp:" + to;
            String from = "whatsapp:+917780033828";

            JSONObject variables = new JSONObject();
            variables.put("product_name", productName);
            variables.put("brand", brand);
            variables.put("from", old_price);
            variables.put("to", new_price);
            variables.put("product_addr", product_url);


            Message message = Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(from),
                            (String) null
                    )
                    .setContentSid("HXccbd7b4277bb67db45486e109142887d") // Your Content SID from Twilio template
                    .setContentVariables(variables.toString())
                    .create();

            logger.info("Sent price alert message, SID: " + message.getSid());
        }
        catch(Exception e){
            logger.error("Error at sendPriceAlertSMS : {}",e.getMessage());
        }
    }

    public void sendStockAlertSMS(String to , String productName,String product_url,String brand){
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            to = "whatsapp:" + to;
            String from = "whatsapp:+917780033828";

            JSONObject variables = new JSONObject();
            variables.put("_name", productName);
            variables.put("brand",brand);
            variables.put("product_addr", product_url);


            Message message = Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(from),
                            (String) null
                    )
                    .setContentSid("HX7d600ef0691f92273dbeaf04d83d53d9") // Your Content SID from Twilio template
                    .setContentVariables(variables.toString())
//                    .setMediaUrl(
//                            Arrays.asList(URI.create(product_image))
//                    )
                    .create();

            logger.info("Sent stock alert message, SID: " + message.getSid());
        }
        catch(Exception e){
            logger.error("Error at sendStockAlertSMS : {}",e.getMessage());
        }
    }


    public static void main(String[] args) {
        TwilioSMS twilioSMS=new TwilioSMS();
        String producturl="https://www.flipkart.com";
        String productImage="https://rukminim2.flixcart.com/image/416/416/xif0q/monitor/m/1/u/-original-imah5c99hmasfzcm.jpeg?q=70&crop=false";
        twilioSMS.sendStockAlertSMS("+919060117328","Monitor Gaming",producturl,"flipkart");
    }


}
