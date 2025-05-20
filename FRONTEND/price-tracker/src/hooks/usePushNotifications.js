import { useState, useEffect } from "react";
import axios from "axios";
import { registerServiceWorker, urlB64ToUint8Array, encodeUint8Array } from "../components/sw.jsx";
import { ENDPOINTS } from "../apicalls/apiConfig.jsx";

const applicationServerPublicKey = import.meta.env.VITE_VAPID_PUBLIC_KEY;

const usePushNotifications = () => {
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [swRegistration, setSwRegistration] = useState(null);
  const [newSubscription, setNewSubscription] = useState(null);

  // Request notification permission
  const requestNotificationPermission = async () => {
    console.log("Requesting notification permission...");

    if (!("Notification" in window)) {
      alert("This browser does not support desktop notifications");
      return false;
    }

    if (Notification.permission === "granted") {
      console.log("Permission already granted");
      return true;
    }

    const permission = await Notification.requestPermission();
    console.log("Permission request result:", permission);
    return permission === "granted";
  };

  // Subscribe to push notifications
  const subscribeFunction = async (email,phoneNumber) => {
    if (!swRegistration) {
      console.error("Service worker registration not found");
      return;
    }

    try {
      const permissionGranted = await requestNotificationPermission();
      if (!permissionGranted) {
        console.log("Notification permission denied");
        return;
      }

      const existingSubscription = await swRegistration.pushManager.getSubscription();
      if (existingSubscription) {
        console.log("Already subscribed:", existingSubscription);
        setIsSubscribed(true);
        return;
      }

      const applicationServerKey = urlB64ToUint8Array(applicationServerPublicKey);
      const subscribeParams = {
        userVisibleOnly: true,
        applicationServerKey,
      };

      const subscription = await swRegistration.pushManager.subscribe(subscribeParams);
      console.log("User is subscribed:", JSON.stringify(subscription));

      const keyArray = subscription.getKey("p256dh");
      const authArray = subscription.getKey("auth");

      if (keyArray && authArray) {
        const encodedKey = encodeUint8Array(new Uint8Array(keyArray));
        const encodedAuth = encodeUint8Array(new Uint8Array(authArray));

        const requestData = {
          publicKey: encodedKey,
          auth: encodedAuth,
          notificationEndPoint: subscription.endpoint,
          email: email,
          phoneNumber: phoneNumber,
        };

        const result = await axios.post(ENDPOINTS.PUSH_NOTIFICATION_SUBSCRIBE_URL, requestData);
        console.log("Subscription result:", result);

        if (result) {
          setIsSubscribed(true);
          setNewSubscription(result.data);
        }
      }
    } catch (error) {
      console.error("Failed to subscribe user:", error);
    }
  };

  // Unsubscribe from push notifications
  const unsubscribeFunction = async () => {
    if (!swRegistration) return;

    try {
      const subscription = await swRegistration.pushManager.getSubscription();
      if (subscription) {
        await subscription.unsubscribe();
        setIsSubscribed(false);
        if (newSubscription?.id) {
          await axios.post(ENDPOINTS.PUSH_NOTIFICATION_UNSUBSCRIBE_URL, { id: newSubscription.id });
        }
        setNewSubscription(null);
      }
    } catch (error) {
      console.error("Error unsubscribing", error);
    }
  };

  // Register service worker on mount
  useEffect(() => {
    registerServiceWorker()
      .then((reg) => {
        if (reg) {
          setSwRegistration(reg);
        }
      })
      .catch((error) => {
        console.error("Service worker registration failed:", error);
      });
  }, []);

  return {
    isSubscribed,
    subscribeFunction,
    unsubscribeFunction,
    requestNotificationPermission,
  };
};

export default usePushNotifications;