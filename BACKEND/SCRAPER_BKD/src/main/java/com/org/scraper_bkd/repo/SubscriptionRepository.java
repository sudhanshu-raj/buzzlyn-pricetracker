package com.org.scraper_bkd.repo;

import com.org.scraper_bkd.model.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<WebPushSubscription,Long> {

    WebPushSubscription findByNotificationEndPoint(String notificationEndPoint);

    List<WebPushSubscription> findByEmailAndPhoneNumber(String email, String phoneNumber);
}
