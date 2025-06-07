package com.org.scraper_bkd.service.notifications;

import com.org.scraper_bkd.service.ProductScraperService;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private final TwilioSMS twilioSMS;





}
