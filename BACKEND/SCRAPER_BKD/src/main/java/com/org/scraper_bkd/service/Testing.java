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

        String product_name= "Fedger® Laptop Cooling Pad with 3 Heavy Duty Fans jkjkjkjkjkjkjkj";
        if(product_name.length()>50){
            product_name=product_name.substring(0,50)+"...";
        }
        System.out.println(product_name);

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

        String productImage="https://m.media-amazon.com/images/I/51LCVDPpqAL._SL1100_.jpg";

        Message message = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(from),
                        (String)null
                )
                .setContentSid("HXb32b9c4b1f0e02641b332b8bdde1147e") // Your Content SID from Twilio template
                .setContentVariables(variables.toString())
                .setMediaUrl(
                        Arrays.asList(URI.create(productImage))
                )
                .create();

        System.out.println("Sent message SID: " + message.getSid());
    }



}
