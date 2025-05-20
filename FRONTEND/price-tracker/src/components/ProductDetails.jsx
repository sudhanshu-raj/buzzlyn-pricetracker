"use client";

import { useState, useEffect } from "react";
import styles from "./ProductDetails.module.css";
import { useAuth } from "../context/AuthContext";
import { trackProduct, fetchTrackerByProductId } from "../apicalls/scraperAPIs";
import { getCurrencySymbol, formatPrice } from "../utils/helperFunctions";
import { useProducts } from "../context/ProductContext";
import { isPincodeTrackingAvailable } from "../utils/helperFunctions";
import usePushNotifications from "../hooks/usePushNotifications";

function ProductDetails({ product, onReset }) {
  const { user } = useAuth();
  const { addProduct } = useProducts();
  const { subscribeFunction } = usePushNotifications();

  const [alertType, setAlertType] = useState("automatic");
  const [notificationType, setNotificationType] = useState(["email"]);
  const [customPrice, setCustomPrice] = useState(
    (product.currentPrice * 0.9).toFixed(2)
  );
  const [updateFrequency, setUpdateFrequency] = useState("weekly");
  const [receiveUpdates, setReceiveUpdates] = useState(false);
  const [customNotificationDays, setCustomNotificationDays] = useState(0);
  const [receivePincodeUpdates, setReceivePincodeUpdates] = useState(false);
  const [pincode, setPincode] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const [isTrackerExisted, setIsTrackerExisted] = useState(false);
  const [userConfigId, setUserConfigId] = useState(0);

  useEffect(() => {
    // Reset error state when product changes
    setError("");
    setSuccess(false);

    // Reset other form states if needed
    setAlertType("automatic");
    setNotificationType(["email"]);
    setCustomPrice((product.currentPrice * 0.9).toFixed(2));
    setUpdateFrequency("weekly");
    setReceiveUpdates(false);
    setReceivePincodeUpdates(false);
    setPincode("");
    setIsTrackerExisted(false);

    const fetchExistedProductTracker = async () => {
      const userRequest = {
        product_id: product.id,
        email: user.email,
        phoneNumber: user.number,
      };

      const response = await fetchTrackerByProductId(userRequest);
      console.log("Fetch Tracker By Product ID Response:", response);
      if (response.success) {
        const configData = response.data;
        console.log("Tracker Data: from product id", configData);
        setNotificationType([]);

        const types = [];
        if (configData.emailSMSEnabled) types.push("email");
        if (configData.phoneSMSEnabled) types.push("sms");
        if (configData.whatsappSMSEnabled) types.push("whatsapp");
        if (configData.pushSMSEnabled) types.push("push");

        setNotificationType(types);

        if (configData.automaticAlert) {
          setAlertType("automatic");
        }
        if (configData.customPriceAlert) {
          setAlertType("custom");
          setCustomPrice(configData.customPrice);
        }
        if (configData.notificationFrequencySet) {
          setReceiveUpdates(true);
          setUpdateFrequency(
            configData.notificationFrequencyValue.toLowerCase()
          );
          if (configData.notificationFrequencyValue === "CUSTOM") {
            setCustomNotificationDays(configData.customNotificationDays);
          }
        }
        if (configData.pincodeStockTracking) {
          setReceivePincodeUpdates(true);
          setPincode(configData.pincode);
        }
        setIsTrackerExisted(true);
        setUserConfigId(configData.id);
      }
    };
    fetchExistedProductTracker();
  }, [product.id]);

  // Reset success state when any input changes
  useEffect(() => {
    if (success) {
      setSuccess(false);
    }
  }, [
    alertType,
    notificationType,
    customPrice,
    updateFrequency,
    receiveUpdates,
    receivePincodeUpdates,
    pincode,
  ]);

  const handleNotificationTypeChange = (type, isChecked) => {
    if (isChecked) {
      setNotificationType([...notificationType, type]);
    } else {
      setNotificationType(notificationType.filter((t) => t !== type));
    }
  };

  const handleNotificationSubmit = async () => {
    // Handle the notification settings submission
    setError("");

    if (notificationType.length === 0) {
      setError("Please select at least one notification channel.");
      return;
    }
    if (alertType === "custom" && !customPrice) {
      setError(
        "Please set a custom price, if you want to track on custom price."
      );
      return;
    }
    if (receivePincodeUpdates && (!pincode || pincode.length !== 6)) {
      setError(
        "Please enter a valid pincode, if you want to track on pincode."
      );
      return;
    }

    let notificationSettings = null;

    if (product.inStock) {
      notificationSettings = {
        productId: product.id,
        userConfigId: userConfigId,
        email: user.email,
        phoneNumber: user.number,
        emailSMSEnabled: notificationType.includes("email"),
        phoneSMSEnabled: notificationType.includes("sms"),
        whatsappSMSEnabled: notificationType.includes("whatsapp"),
        pushSMSEnabled: notificationType.includes("push"),
        automaticAlert: alertType === "automatic",
        customPriceAlert: alertType === "custom",
        customPrice: alertType === "custom" ? customPrice : null,
        stockAlert: false,
        pincodeStockTracking: receivePincodeUpdates,
        pincode: receivePincodeUpdates ? pincode : null,
        notificationFrequencySet: receiveUpdates,
        notificationFrequencyValue: receiveUpdates
          ? updateFrequency.toUpperCase()
          : null,
        customNotificationDays:
          receiveUpdates && updateFrequency === "custom"
            ? customNotificationDays
            : null,
      };
    } else {
      notificationSettings = {
        productId: product.id,
        email: user.email,
        phoneNumber: user.number,
        emailSMSEnabled: notificationType.includes("email"),
        phoneSMSEnabled: notificationType.includes("sms"),
        whatsappSMSEnabled: notificationType.includes("whatsapp"),
        pushSMSEnabled: notificationType.includes("push"),
        automaticAlert: false,
        customPriceAlert: false,
        customPrice: 0,
        stockAlert: true,
        pincodeStockTracking: false,
        pincode: null,
        notificationFrequencySet: false,
        notificationFrequencyValue: null,
        customNotificationDays: null,
      };
    }
    console.log("Notification Settings:", notificationSettings);

    const response = await trackProduct(notificationSettings);
    console.log("Track Product  raw Response:", response);

    if (!response.success) {
      setError(response.error || "Failed to track product");
      return;
    }
    if (notificationSettings.pushSMSEnabled) {
      const subscription = await subscribeFunction(user.email, user.number);
      console.log("Push Notification Subscription:", subscription);
    }

    //here we will add the product to the dashboard
    const rawData = response.data;
    const dashboardData = {
      id: rawData.id,
      name: rawData.productName,
      image: rawData.imageURL || "/placeholder.svg?height=300&width=300",
      description: null,
      currency: rawData.currency,
      store: rawData.brand,
      currentPrice: rawData.price,
      originalPrice: rawData.mrp,
      discount:
        rawData.price < rawData.mrp
          ? (((rawData.mrp - rawData.price) / rawData.mrp) * 100).toFixed(2)
          : 0,
      target: rawData.userTargetStatus,
      stock_status: rawData.stock_status === "in_stock",
      running_status: rawData.running_status === "TRACKING",
    };
    addProduct(dashboardData);
    setSuccess(true);
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Product Details</h2>
        <button onClick={onReset} className={styles.resetButton}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className={styles.resetIcon}
          >
            <path d="M2.5 2v6h6M21.5 22v-6h-6" />
            <path d="M22 11.5A10 10 0 0 0 3.2 7.2M2 12.5a10 10 0 0 0 18.8 4.2" />
          </svg>
          Track Another Product
        </button>
      </div>

      <div className={styles.card}>
        <div className={styles.productGrid}>
          <div className={styles.imageContainer}>
            <img
              src={product.image || "/placeholder.svg"}
              alt={product.title}
              className={styles.productImage}
            />
          </div>
          <div className={styles.productInfo}>
            <h3 className={styles.productTitle}>{product.title}</h3>

            <div className={styles.storeInfo}>
              <span className={styles.store}>{product.store}</span>
              <a
                href={product.url}
                target="_blank"
                rel="noopener noreferrer"
                className={styles.viewLink}
              >
                View Product
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className={styles.externalIcon}
                >
                  <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                  <polyline points="15 3 21 3 21 9" />
                  <line x1="10" x2="21" y1="14" y2="3" />
                </svg>
              </a>
            </div>

            <div className={styles.priceContainer}>
              <div className={styles.currentPrice}>
                {formatPrice(product.currentPrice, product.currency)}
              </div>
              {product.originalPrice > product.currentPrice && (
                <div className={styles.originalPrice}>
                  {formatPrice(product.originalPrice, product.currency)}
                </div>
              )}
              {product.discount > 0 && (
                <div className={styles.discount}>{product.discount}% OFF</div>
              )}
            </div>

            <div className={styles.ratingStockContainer}>
              <div className={styles.rating}>
                <div className={styles.stars}>
                  {Array.from({ length: 5 }).map((_, i) => (
                    <svg
                      key={i}
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill={
                        i < Math.floor(product.rating) ? "currentColor" : "none"
                      }
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.star}
                    >
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                    </svg>
                  ))}
                </div>
                <span className={styles.ratingValue}>{product.rating}</span>
                <span className={styles.reviewCount}>
                  ({product.reviewCount} reviews)
                </span>
              </div>

              <div className={styles.availability}>
                {product.inStock ? (
                  <span className={styles.inStock}>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.stockIcon}
                    >
                      <path d="M20 6 9 17l-5-5" />
                    </svg>
                    In Stock
                  </span>
                ) : (
                  <span className={styles.outOfStock}>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.stockIcon}
                    >
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                    Out of Stock
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className={styles.notificationsContent}>
          <div className={styles.notificationCard}>
            {!success ? (
              <>
                <div className={styles.cardHeader}>
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className={styles.headerIcon}
                  >
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                  </svg>
                  <h4 className={styles.cardTitle}>Notification Settings</h4>
                </div>

                <div className={styles.notificationSection}>
                  <h5 className={styles.sectionTitle}>Notification Channels</h5>
                  <div className={styles.notificationOptions}>
                    <div className={styles.notificationOption}>
                      <input
                        type="checkbox"
                        id="email-notification"
                        className={styles.checkbox}
                        checked={notificationType.includes("email")}
                        onChange={(e) =>
                          handleNotificationTypeChange(
                            "email",
                            e.target.checked
                          )
                        }
                      />
                      <label
                        htmlFor="email-notification"
                        className={styles.checkboxLabel}
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          className={styles.channelIcon}
                        >
                          <rect x="2" y="4" width="20" height="16" rx="2" />
                          <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                        </svg>
                        Email
                      </label>
                    </div>
                    <div className={styles.notificationOption}>
                      <input
                        type="checkbox"
                        id="sms-notification"
                        className={styles.checkbox}
                        checked={notificationType.includes("sms")}
                        onChange={(e) =>
                          handleNotificationTypeChange("sms", e.target.checked)
                        }
                      />
                      <label
                        htmlFor="sms-notification"
                        className={styles.checkboxLabel}
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          className={styles.channelIcon}
                        >
                          <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
                        </svg>
                        SMS
                      </label>
                    </div>
                    <div className={styles.notificationOption}>
                      <input
                        type="checkbox"
                        id="whatsapp-notification"
                        className={styles.checkbox}
                        checked={notificationType.includes("whatsapp")}
                        onChange={(e) =>
                          handleNotificationTypeChange(
                            "whatsapp",
                            e.target.checked
                          )
                        }
                      />
                      <label
                        htmlFor="whatsapp-notification"
                        className={styles.checkboxLabel}
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          className={styles.channelIcon}
                        >
                          <path d="M3 21l1.65-3.8a9 9 0 1 1 3.4 2.9L3 21" />
                          <path d="M9 10a.5.5 0 0 0 1 0V9a.5.5 0 0 0-1 0v1Z" />
                          <path d="M14 10a.5.5 0 0 0 1 0V9a.5.5 0 0 0-1 0v1Z" />
                          <path d="M9 14a.5.5 0 0 0 .5.5h5a.5.5 0 0 0 0-1h-5a.5.5 0 0 0-.5.5Z" />
                        </svg>
                        WhatsApp
                      </label>
                    </div>
                    <div className={styles.notificationOption}>
                      <input
                        type="checkbox"
                        id="push-notification"
                        className={styles.checkbox}
                        checked={notificationType.includes("push")}
                        onChange={(e) =>
                          handleNotificationTypeChange("push", e.target.checked)
                        }
                      />
                      <label
                        htmlFor="push-notification"
                        className={styles.checkboxLabel}
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          className={styles.channelIcon}
                        >
                          <path d="M18 8a6 6 0 0 0-6-6 6 6 0 0 0-6 6c0 7-3 9-3 9h18s-3-2-3-9" />
                          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                        </svg>
                        Push Notifications
                      </label>
                    </div>
                  </div>
                </div>

                {!product.inStock ? (
                  <div className={styles.notificationSection}>
                    <div className={styles.stockAlertBanner}>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className={styles.bannerIcon}
                      >
                        <path d="M6 8.32a7.43 7.43 0 0 1 0 7.36" />
                        <path d="M9.46 6.21a11.76 11.76 0 0 1 0 11.58" />
                        <path d="M12.91 4.1a15.91 15.91 0 0 1 .01 15.8" />
                        <path d="M16.37 2a20.16 20.16 0 0 1 0 20" />
                      </svg>
                      <div>
                        <h5 className={styles.bannerTitle}>
                          Product Out of Stock
                        </h5>
                        <p className={styles.bannerText}>
                          We'll notify you when this product is back in stock.
                        </p>
                      </div>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className={styles.notificationSection}>
                      <h5 className={styles.sectionTitle}>
                        Price Alert Settings
                      </h5>
                      <div className={styles.alertTypeOptions}>
                        <div className={styles.radioOption}>
                          <input
                            type="radio"
                            id="automatic-alert"
                            name="alert-type"
                            className={styles.radio}
                            checked={alertType === "automatic"}
                            onChange={() => setAlertType("automatic")}
                          />
                          <label
                            htmlFor="automatic-alert"
                            className={styles.radioLabel}
                          >
                            <div className={styles.radioLabelContent}>
                              <span className={styles.boldText}>
                                Automatic Alert
                              </span>
                              <span className={styles.helpText}>
                                Notify me of any price drop
                              </span>
                            </div>
                          </label>
                        </div>
                        <div className={styles.radioOption}>
                          <input
                            type="radio"
                            id="custom-alert"
                            name="alert-type"
                            className={styles.radio}
                            checked={alertType === "custom"}
                            onChange={() => setAlertType("custom")}
                          />
                          <label
                            htmlFor="custom-alert"
                            className={styles.radioLabel}
                          >
                            <div className={styles.radioLabelContent}>
                              <span className={styles.boldText}>
                                Custom Price Alert
                              </span>
                              <span className={styles.helpText}>
                                Set your target price
                              </span>
                            </div>
                          </label>
                        </div>
                      </div>

                      {alertType === "custom" && (
                        <div className={styles.customPriceContainer}>
                          <div className={styles.inputGroup}>
                            <span className={styles.inputPrefix}>
                              {getCurrencySymbol(product.currency)}
                            </span>
                            <input
                              type="number"
                              id="custom-price"
                              className={styles.input}
                              placeholder="0.00"
                              value={customPrice}
                              onChange={(e) => setCustomPrice(e.target.value)}
                              min="1"
                              step="0.01"
                            />
                          </div>
                          <p className={styles.inputHelp}>
                            Current price:{" "}
                            {formatPrice(
                              product.currentPrice,
                              product.currency
                            )}
                            . You'll be notified when the price drops to or
                            below your custom price.
                          </p>
                          <div className={styles.discountButtons}>
                            {[5, 10, 15, 20, 25].map((discount) => (
                              <button
                                key={discount}
                                className={styles.discountButton}
                                onClick={() => {
                                  setCustomPrice(
                                    (
                                      product.currentPrice *
                                      (1 - discount / 100)
                                    ).toFixed(2)
                                  );
                                }}
                              >
                                {discount}% OFF
                              </button>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>

                    {isPincodeTrackingAvailable(product.store) && (
                      <div className={styles.pincodeSection}>
                        <div
                          className={styles.updateToggleContainer}
                          style={{ margin: "0" }}
                        >
                          <div className={styles.updateToggle}>
                            <input
                              type="checkbox"
                              id="pincode-track"
                              className={[styles.checkbox,styles.pincodeCheckbox].join(" ")}
                              checked={receivePincodeUpdates}
                              onChange={() =>
                                setReceivePincodeUpdates(!receivePincodeUpdates)
                              }
                            />
                            <label
                              htmlFor="pincode-track"
                              className={styles.checkboxLabel}
                              style={{ alignItems: "flex-start" }}
                            >
                              <span className={styles.boldText}>
                                Track Product Stock on Pincode
                              </span>
                              <span
                                className={styles.helpText}
                                style={{ marginTop: "-0.25rem" }}
                              >
                                If the product is not available in your area,
                                we'll notify you
                              </span>
                            </label>
                          </div>
                        </div>

                        {receivePincodeUpdates && (
                          <div
                            className={`${styles.pincodeSection} ${styles.updateFrequencyContainer}`}
                          >
                            <h5 className={styles.sectionTitle}>
                              Set your pincode{" "}
                            </h5>
                            <input
                              type="number"
                              id="custom-price"
                              className={`${styles.pincodeSection} ${styles.input}`}
                              placeholder="123456"
                              value={pincode}
                              onChange={(e) => setPincode(e.target.value)}
                              min="100000"
                              max="999999"
                            />
                          </div>
                        )}
                      </div>
                    )}

                    <div className={styles.notificationSection}>
                      <div className={styles.updateToggleContainer}>
                        <div className={styles.updateToggle}>
                          <input
                            type="checkbox"
                            id="receive-updates"
                            className={[styles.checkbox,styles.updateCheckbox].join(" ")}
                            checked={receiveUpdates}
                            onChange={() => setReceiveUpdates(!receiveUpdates)}
                          />
                          <label
                            htmlFor="receive-updates"
                            className={styles.checkboxLabel}
                            style={{ alignItems: "flex-start" }}
                          >
                            <span className={styles.boldText}>
                              Receive regular product updates
                            </span>
                            <span
                              className={styles.helpText}
                              style={{ marginTop: "-0.25rem" }}
                            >
                              Get notified about price increases, stock changes,
                              and other product updates
                            </span>
                          </label>
                        </div>
                      </div>

                      {receiveUpdates && (
                        <div className={styles.updateFrequencyContainer}>
                          <h5 className={styles.sectionTitle}>
                            Update Frequency
                          </h5>
                          <p className={styles.sectionDescription}>
                            How often would you like to receive product updates?
                          </p>
                          <div className={styles.frequencyOptions}>
                            <div className={styles.frequencyOption}>
                              <input
                                type="radio"
                                id="weekly-updates"
                                name="update-frequency"
                                className={styles.radio}
                                checked={updateFrequency === "weekly"}
                                onChange={() => setUpdateFrequency("weekly")}
                              />
                              <label
                                htmlFor="weekly-updates"
                                className={styles.frequencyLabel}
                              >
                                <div className={styles.frequencyHeader}>
                                  <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    width="18"
                                    height="18"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    className={styles.frequencyIcon}
                                  >
                                    <rect
                                      x="3"
                                      y="4"
                                      width="18"
                                      height="18"
                                      rx="2"
                                      ry="2"
                                    />
                                    <line x1="16" y1="2" x2="16" y2="6" />
                                    <line x1="8" y1="2" x2="8" y2="6" />
                                    <line x1="3" y1="10" x2="21" y2="10" />
                                  </svg>
                                  <span>Weekly</span>
                                </div>
                                <span className={styles.frequencyDescription}>
                                  Get updates once a week
                                </span>
                              </label>
                            </div>
                            <div className={styles.frequencyOption}>
                              <input
                                type="radio"
                                id="monthly-updates"
                                name="update-frequency"
                                className={styles.radio}
                                checked={updateFrequency === "monthly"}
                                onChange={() => setUpdateFrequency("monthly")}
                              />
                              <label
                                htmlFor="monthly-updates"
                                className={styles.frequencyLabel}
                              >
                                <div className={styles.frequencyHeader}>
                                  <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    width="18"
                                    height="18"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    className={styles.frequencyIcon}
                                  >
                                    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                                    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                                  </svg>
                                  <span>Monthly</span>
                                </div>
                                <span className={styles.frequencyDescription}>
                                  Get updates once a month
                                </span>
                              </label>
                            </div>

                            <div className={styles.frequencyOption}>
                              <input
                                type="radio"
                                id="custom-days"
                                name="update-frequency"
                                className={styles.radio}
                                checked={updateFrequency === "custom"}
                                onChange={() => setUpdateFrequency("custom")}
                              />
                              <label
                                htmlFor="custom-days"
                                className={styles.frequencyLabel}
                              >
                                <div className={styles.frequencyHeader}>
                                  <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    width="18"
                                    height="18"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    className={styles.frequencyIcon}
                                  >
                                    <circle cx="12" cy="12" r="10" />
                                    <polyline points="12 6 12 12 16 14" />
                                  </svg>
                                  <span>Custom Days</span>
                                </div>
                            
                                {updateFrequency === "custom" ? (
                                  <div
                                    className={styles.customDaysInputContainer}
                                    style={{
                                      marginTop: "0px",
                                      marginLeft: "20px",
                                      display: "flex",
                                      alignItems: "center",
                                      
                                    }}
                                  >
                                     <input
                                        type="number"
                                        value={customNotificationDays } 
                                        onChange={(e) =>
                                          setCustomNotificationDays(
                                            parseInt(e.target.value) 
                                          )
                                        }
                                        min="1"
                                        max="365"
                                        className={styles.customDaysInput}
                                        style={{
                                          width: "60px",
                                          height: "20px",
                                          padding: "0px 8px",
                                          border: "1px solid #ccc", // Direct color instead of CSS variable
                                          borderRadius: "4px",
                                          fontSize: "14px",
                                          backgroundColor: "#ffffff", 
                                          color: "#000000", 
                                          display: "block", 
                                          opacity: 1, 
                                          position: "relative", 
                                          zIndex: 5 ,
                                          
                                        }}
                                      />
                                  </div>
                                ):(
                                   <span className={styles.frequencyDescription}>
                                  Custom days for updates
                                </span>
                                )}
                              </label>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  </>
                )}
                {isTrackerExisted && (
                  <p className={styles.infoMessage}>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.errorIcon}
                    >
                      <path d="M12 9v2m0 4h.01" />
                      <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" />
                    </svg>
                    Note : This product is already being tracked. Saving this
                    will override the previous settings.
                  </p>
                )}
                {error && (
                  <p className={styles.errorMessage}>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.errorIcon}
                    >
                      <path d="M12 9v2m0 4h.01" />
                      <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" />
                    </svg>
                    {error}
                  </p>
                )}

                <div className={styles.notificationFooter}>
                  <button
                    className={styles.saveButton}
                    onClick={handleNotificationSubmit}
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.buttonIcon}
                    >
                      <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                      <polyline points="17 21 17 13 7 13 7 21" />
                      <polyline points="7 3 7 8 15 8" />
                    </svg>
                    Save Notification Settings
                  </button>
                </div>
              </>
            ) : (
              <div className={styles.successContainer}>
                <div className={styles.successHeader}>
                  <div className={styles.successIconContainer}>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="40"
                      height="40"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className={styles.successIcon}
                    >
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                      <polyline points="22 4 12 14.01 9 11.01" />
                    </svg>
                  </div>
                  <div className={styles.confetti}>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                    <div className={styles.confettiPiece}></div>
                  </div>
                </div>

                <div className={styles.successContent}>
                  <h3 className={styles.successTitle}>
                    Product Tracking Activated!
                  </h3>
                  <p className={styles.successDescription}>
                    {/* We've set up price tracking for <span className={styles.productHighlight}>{product.title}</span>. */}
                    You'll be notified when there are updates based on your
                    preferences.
                  </p>

                  <div className={styles.successActions}>
                    <a href="/dashboard" className={styles.dashboardLink}>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="18"
                        height="18"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className={styles.actionIcon}
                      >
                        <rect
                          x="3"
                          y="3"
                          width="18"
                          height="18"
                          rx="2"
                          ry="2"
                        />
                        <line x1="3" y1="9" x2="21" y2="9" />
                        <line x1="9" y1="21" x2="9" y2="9" />
                      </svg>
                      View in Dashboard
                    </a>
                    <button
                      onClick={onReset}
                      className={styles.trackAnotherButton}
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="18"
                        height="18"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className={styles.actionIcon}
                      >
                        <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                        <polyline points="17 21 17 13 7 13 7 21" />
                        <polyline points="7 3 7 8 15 8" />
                      </svg>
                      Track Another Product
                    </button>
                  </div>

                  <p className={styles.successNote}>
                    You can modify your tracking preferences anytime from your
                    dashboard.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetails;
