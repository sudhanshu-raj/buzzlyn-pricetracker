package com.org.scraper_bkd.service.notifications;

import com.org.scraper_bkd.service.ProductScraperService;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(ProductScraperService.class);
    @Value("${twilio.phone_number}")
    private String twilioPhoneNumber;

    public String sendSms(String to, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            return message.getSid();
        }// Return message SID as confirmation
        catch (TwilioException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
