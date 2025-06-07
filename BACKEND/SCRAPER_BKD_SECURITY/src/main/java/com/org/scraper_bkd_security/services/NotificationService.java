package com.org.scraper_bkd_security.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.*;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public static final String ACCOUNT_SID =TWILIO_ACCOUNT_SID;
    public static final String AUTH_TOKEN = TWILIO_AUTH_TOKEN;

    public void sendAuthenticationOTPEmail(String otp,String to){
        try{
            if (otp.length()!=4) {
                logger.error("OTP must be four digits to send email");
                return;
            };
            String htmlTemplate = EmailService.loadTemplate("authenticationOTP.template");
            String finalHtml=htmlTemplate
                    .replace("{logo_URL}",BUZZLYN_LOGO)
                    .replace("{1}", String.valueOf(otp.charAt(0)))
                    .replace("{2}", String.valueOf(otp.charAt(1)))
                    .replace("{3}", String.valueOf(otp.charAt(2)))
                    .replace("{4}", String.valueOf(otp.charAt(3)));

            String subject="Authentication OTP for buzzlyn";
            EmailService.sendEmail(to,subject,finalHtml);
            logger.info("Authentication otp email  sent to : {}",to);
        }
        catch(Exception e){
            logger.error("Error at sendAuthenticationOTP : {}",e.getMessage());
            throw new RuntimeException("Error while sending OTP");
        }
    }

    public void sendPasswordRecoveryOTPEmail(String otp,String to){
        try{
            if (otp.length()!=4) {
                logger.error("OTP must be four digits to send email");
                return;
            };
            String htmlTemplate = EmailService.loadTemplate("passwordRecoveryOTP.template");
            String finalHtml=htmlTemplate
                    .replace("{logo_URL}",BUZZLYN_LOGO)
                    .replace("{1}", String.valueOf(otp.charAt(0)))
                    .replace("{2}", String.valueOf(otp.charAt(1)))
                    .replace("{3}", String.valueOf(otp.charAt(2)))
                    .replace("{4}", String.valueOf(otp.charAt(3)));

            String subject="Password Reset OTP for buzzlyn";
            EmailService.sendEmail(to,subject,finalHtml);
            logger.info("Password reset otp email sent to : {}",to);
        }
        catch(Exception e){
            logger.error("Error at sendPasswordRecoveryOTPEmail : {}",e.getMessage());
            throw new RuntimeException("Error while sending OTP");
        }
    }


    public void sendAuthenticationOTPWBSMS(String to ,String otp){
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            String from = "whatsapp:+917780033828";

            Message message = Message
                    .creator(new PhoneNumber("whatsapp:"+to),
                            new PhoneNumber(from),
                            (String) null
                    )
                    .setContentSid("HX71dab39bf1c0283976421ddd4c147ebb")
                    .setContentVariables(new JSONObject(new HashMap<String, Object>() {
                        {
                            put("1", otp);
                        }
                    }).toString())
                    .create();

            logger.info("Authentication OTP sent of SID : {} ",message.getSid());
        }
        catch(Exception e){
            logger.error("Error at sendAuthenticationOTP : {}",e.getMessage());
            throw new RuntimeException("Error while sending OTP");
        }

    }

    public static void main(String[] args) {
        NotificationService obj=new NotificationService();
        obj.sendPasswordRecoveryOTPEmail("4521","rajsudhanshu9431@gmail.com");
    }

}
