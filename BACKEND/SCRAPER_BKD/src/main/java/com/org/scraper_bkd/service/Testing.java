package com.org.scraper_bkd.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.cloudinary.json.JSONObject;

import java.util.HashMap;

import static com.org.scraper_bkd.constants.AppConstant.TWILIO_ACCOUNT_SID;
import static com.org.scraper_bkd.constants.AppConstant.TWILIO_AUTH_TOKEN;


public class Testing {

    public static final String ACCOUNT_SID =TWILIO_ACCOUNT_SID;
    public static final String AUTH_TOKEN = TWILIO_AUTH_TOKEN;

    public static void main(String[] args) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String to="whatsapp:+919060117328";
        String from ="whatsapp:+917780033828";

        Message message = Message
                .creator(new PhoneNumber(to),
                        new PhoneNumber(from),
                        (String)null
                        )
                .setContentSid("HX71dab39bf1c0283976421ddd4c147ebb")
                .setContentVariables(new JSONObject(new HashMap<String, Object>() {
                    {
                        put("1", "3331");
                    }
                }).toString())
                .create();

        System.out.println(message.getBody());

    }
}
