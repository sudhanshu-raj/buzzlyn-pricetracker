package com.org.scraper_bkd.controller;

import com.org.scraper_bkd.model.WebPushMessage;
import com.org.scraper_bkd.model.WebPushSubscription;
import com.org.scraper_bkd.service.notifications.PushSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jose4j.lang.JoseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class WebPushController {

    private final PushSubscriptionService subscriptionService;


    @PostMapping("/subscribe")
    public void subscribe(@RequestBody WebPushSubscription webPushSubscription){
        System.out.println("subscribe is hit ");
        subscriptionService.saveSubscription(webPushSubscription);
    }

    @PostMapping("/unsubscribe")
    public void unsubscribe(@RequestBody Long id){
        System.out.println("unsubscribe api hit");
        subscriptionService.deleteSubscription(id);
    }

    @PostMapping("/notifyAll")
    public ResponseEntity<String> notifyAll(@RequestBody WebPushMessage message) throws JoseException, GeneralSecurityException, IOException, ExecutionException, InterruptedException, JoseException, GeneralSecurityException, IOException, ExecutionException {
        System.out.println("notifyAll api bit");
        return subscriptionService.notifyAll(message);
    }
}
