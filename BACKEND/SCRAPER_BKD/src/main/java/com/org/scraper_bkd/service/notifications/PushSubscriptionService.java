package com.org.scraper_bkd.service.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.scraper_bkd.model.WebPushMessage;
import com.org.scraper_bkd.model.WebPushSubscription;
import com.org.scraper_bkd.repo.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.org.scraper_bkd.constants.AppConstant.*;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PushSubscriptionService.class);

//    private static final String PUBLIC_KEY = "BLzc_LUkNVhLWv-8lDqgpG2yPMWCxZb6h5SFSqvK-_rt3P-UxXg9AUra1Vy-tM6hudJNqHGR4L-__H5X6Sssp88";
//    private static final String PRIVATE_KEY = "Sn_z2AAo6qYs_PActNrzyWWE4VkAWhiLQwpwsF5kgx8";
//    private static final String SUBJECT = "mailto:rajsudhanshu9431@gmail.com";

    private final ObjectMapper objectMapper;
    private final SubscriptionRepository subscriptionRepository;



    public void saveSubscription(WebPushSubscription subscription){
        subscriptionRepository.save(subscription);
    }

    public void deleteSubscription(Long id){
        subscriptionRepository.deleteById(id);
    }

    public WebPushSubscription findByNotificationEndPoint(String notification){
        return subscriptionRepository.findByNotificationEndPoint(notification);
    }

    public ResponseEntity<String> notifyAll(WebPushMessage message) throws GeneralSecurityException, IOException, JoseException, ExecutionException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());
        List<WebPushSubscription> subscriptionList = subscriptionRepository.findAll();
        PushService pushService = new PushService(WEBPUSH_PUBLIC_KEY, WEBPUSH_PRIVATE_KEY, WEBPUSH_MAIL);
        int successCount = 0;
        int failCount = 0;

        for (WebPushSubscription subscription : subscriptionList) {
            try {
                // Validate subscription data before creating notification
                if (subscription.getNotificationEndPoint() == null ||
                        subscription.getPublicKey() == null ||
                        subscription.getAuth() == null) {

                    logger.warn("Skipping invalid subscription: missing required fields. ID: {}",
                            subscription.getId());
                    failCount++;
                    continue;
                }

                Notification notification = new Notification(
                        subscription.getNotificationEndPoint(),
                        subscription.getPublicKey(),
                        subscription.getAuth(),
                        objectMapper.writeValueAsBytes(message)
                );

                pushService.send(notification);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to send notification to subscription ID {}: {}",
                        subscription.getId(), e.getMessage());
                failCount++;
            }
        }

        logger.info("Push notifications summary: {} successful, {} failed", successCount, failCount);
        return ResponseEntity.ok("Notifications sent: " + successCount + " successful, " + failCount + " failed");
    }
}
