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
       Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String to="whatsapp:+919060117328";
        String from ="whatsapp:+917780033828";

        JSONObject variables = new JSONObject();
        variables.put("product_name", "Fedger® Laptop Cooling Pad with 3 Heavy Duty Fans");
        variables.put("brand", "amazon");
        variables.put("old_price", "Rs 2,222");
        variables.put("new_price","Rs 1,120");
        variables.put("product_url", "https://amzn.to/43F8yC8");

        String productImage="https://m.media-amazon.com/images/W/MEDIAX_1215821-T2/images/I/71TySnhUSFL._SL1500_.jpg";

        Message message = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(from),
                        (String)null
                )
                .setContentSid("HXe850e852874c7dcd60bf0dd1af585c6e") // Your Content SID from Twilio template
                .setContentVariables(variables.toString())
                .setMediaUrl(
                        Arrays.asList(URI.create(productImage))
                )
                .create();

        System.out.println("Sent message SID: " + message.getSid());


    }


        public static String CreateTemplate() {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            var waCard = new Content.WhatsappCard();
            waCard.setBody("Hello, Good news! The price of {{1}} on {{2}} has just dropped to *{{3}}*. It’s a great deal—grab it now before the price goes up again!");
            waCard.setMedia(List.of("https://m.media-amazon.com/images/W/MEDIAX_1215821-T2/images/I/71TySnhUSFL._SL1500_.jpg"));

            waCard.setFooter("To unsubscribe, reply Stop");

            var action1 = new Content.CardAction();
            action1.setType(Content.CardActionType.URL);
            action1.setUrl("https://www.twilio.com");
            action1.setTitle("Visit Website");


            waCard.setActions(Arrays.asList(action1));

            var types = new Content.Types();
            types.setWhatsappCard(waCard);

            var createRequest = new Content.ContentCreateRequest("en", types);
            createRequest.setFriendlyName("owl_coupon_code");
            createRequest.setVariables(Map.of(
                    "1", "Fedger® Laptop Cooling Pad with 3 Heavy Duty Fans ","2","amazon","3","Rs 21,200"
            ));

            var content = Content.creator(createRequest).create();

            return content.getSid();
        }
}
