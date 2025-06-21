package com.org.scraper_bkd.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.content.v1.Content;
import com.twilio.type.PhoneNumber;
import org.cloudinary.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

import static com.org.scraper_bkd.constants.AppConstant.TWILIO_ACCOUNT_SID;
import static com.org.scraper_bkd.constants.AppConstant.TWILIO_AUTH_TOKEN;


public class Testing {

    public static final String ACCOUNT_SID =TWILIO_ACCOUNT_SID;
    public static final String AUTH_TOKEN = TWILIO_AUTH_TOKEN;

    public static void main(String[] args) throws UnsupportedEncodingException {

       String brand="amazon.com";
        System.out.println((brand.split("\\.")[0]));

    }

    static void twilioTest(){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String to="whatsapp:+919060117328";
        String from ="whatsapp:+917780033828";

        JSONObject variables = new JSONObject();
        variables.put("product_name", "Fedger® Laptop Cooling Pad with 3 Heavy Duty Fans");
//        variables.put("brand", "amazon");
//        variables.put("from", "Rs 2,222");
//        variables.put("to","Rs 1,120");
        variables.put("product_url", "https://amzn.to/43F8yC8");

        String productImage="https://rukminim2.flixcart.com/image/416/416/xif0q/monitor/m/1/u/-original-imah5c99hmasfzcm.jpeg?q=70&crop=false";

        Message message = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(from),
                        (String)null
                )
                .setContentSid("HX38f88c44d7c6553a453aced8372aa763") // Your Content SID from Twilio template
                .setContentVariables(variables.toString())
                .setMediaUrl(
                        Arrays.asList(URI.create(productImage))
                )
                .create();

        System.out.println("Sent message SID: " + message.getSid());
    }



}
