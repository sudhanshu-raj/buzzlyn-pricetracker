package com.org.scraper_bkd.service.notifications;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.org.scraper_bkd.constants.AppConstant.TWILIO_ACCOUNT_SID;
import static com.org.scraper_bkd.constants.AppConstant.TWILIO_AUTH_TOKEN;

@Component
public class TwilioSMS {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSMS.class);

    public static final String ACCOUNT_SID =TWILIO_ACCOUNT_SID;
    public static final String AUTH_TOKEN = TWILIO_AUTH_TOKEN;



}
