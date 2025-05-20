/*
This is the product details page which will appear when user clicks on product from their dashboard
*/

import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import styles from "./DashboardProductDetails.module.css";
import { getCurrencySymbol } from "../utils/helperFunctions";
import { formatPrice } from "../utils/helperFunctions";
import Loading from "./Loading";
import { updateUserConfig } from "../apicalls/scraperAPIs";
import { getErrorMessage } from "../services/handleErrorMssg";
import { useAuth } from "../context/AuthContext";
import { useProducts } from "../context/ProductContext";
import ProductDataChart from "./ProductDataChart";
import { isPincodeTrackingAvailable } from "../utils/helperFunctions";
import usePushNotifications from "../hooks/usePushNotifications";

function DashboardProductDetails({
  product = product,
  configData = configData,
  setConfigData = setConfigData,
  activeTab = activeTab,
  isConfigLoading = isConfigLoading,
}) {
  const { user } = useAuth();
  const { refreshProducts } = useProducts();
  const { subscribeFunction, unsubscribeFunction } = usePushNotifications();

  const [notificationType, setNotificationType] = useState([]);
  const [alertType, setAlertType] = useState(null);
  const [customPrice, setCustomPrice] = useState(0);
  const [updateFrequency, setUpdateFrequency] = useState(null);
  const [customNotificationDays, setCustomNotificationDays] = useState(0);
  const [receiveUpdates, setReceiveUpdates] = useState(false);
  const [receivePincodeUpdates, setReceivePincodeUpdates] = useState(false);
  const [pincode, setPincode] = useState(null);
  const [isStockAlertEnabled, setIsStockAlertEnabled] = useState(false);

  const [error, setError] = useState("");
  const [configSaved, setConfigSaved] = useState(false);

  useEffect(() => {
    if (configData) {
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
        setUpdateFrequency(configData.notificationFrequencyValue);
        if (configData.notificationFrequencyValue === "CUSTOM") {
          setCustomNotificationDays(configData.customNotificationDays);
        }
      }
      if (configData.pincodeStockTracking) {
        setReceivePincodeUpdates(true);
        setPincode(configData.pincode);
      }
      if (configData.stockAlert) {
        setIsStockAlertEnabled(true);
      }
    }
  }, [configData]);

  const handleNotificationTypeChange = (type, isChecked) => {
    if (isChecked) {
      setNotificationType([...notificationType, type]);
    } else {
      setNotificationType(notificationType.filter((t) => t !== type));
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    }).format(date);
  };

  const handleConfigSave = async () => {
    setError("");
    setConfigSaved(false);

    if (notificationType.length === 0) {
      setError("Please select at least one notification type.");
      return;
    }
    if (alertType === "custom" && customPrice <= 0) {
      setError("Please enter a valid custom price.");
      return;
    }
    if (receivePincodeUpdates && pincode.length !== 6) {
      setError("Please enter a valid pincode.");
      return;
    }

    const config = {
      id: configData.id,
      email: user?.email,
      phoneNumber: user?.number,
      emailSMSEnabled: notificationType.includes("email"),
      phoneSMSEnabled: notificationType.includes("sms"),
      whatsappSMSEnabled: notificationType.includes("whatsapp"),
      pushSMSEnabled: notificationType.includes("push"),
      automaticAlert: alertType === "automatic",
      customPriceAlert: alertType === "custom",
      customPrice: customPrice ? customPrice : 0,
      notificationFrequencySet: receiveUpdates,
      notificationFrequencyValue: updateFrequency,
      customNotificationDays: receiveUpdates && updateFrequency === "CUSTOM" && customNotificationDays
        ? customNotificationDays
        : 0,
      pincodeStockTracking: receivePincodeUpdates,
      pincode: pincode ? pincode : null,
      stockAlert: isStockAlertEnabled,
    };

    console.log("Config to be saved:", config);
    const response = await updateUserConfig(config);
    console.log("Response from updateUserConfig:", response);
    if (!response.success) {
      const errorMsg = getErrorMessage(
        response.error,
        "Failed to update user config"
      );
      setError(errorMsg);
      console.error("Error updating user config:", errorMsg);
      return;
    }

    if (config.pushSMSEnabled && !configData.pushSMSEnabled) {
      const subscription = await subscribeFunction(user.email, user.number);
      console.log("Push Notification Subscription:", subscription);
    }

    setConfigData(config);
    setConfigSaved(true);
    refreshProducts();

    setTimeout(() => {
      setConfigSaved(false);
    }, 2000);
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}></div>

      <div className={styles.card}>
        <div className={styles.productGrid}>
          <div className={styles.imageContainer}>
            <img
              src={product.image || "/placeholder.svg"}
              alt={product.name}
              className={styles.productImage}
            />
          </div>
          <div className={styles.productInfo}>
            <h3 className={styles.productTitle}>{product.name}</h3>

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

            <div className={styles.availability}>
              {product.stock_status ? (
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
                      i < Math.floor(product.ratings) ? "currentColor" : "none"
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
              <span className={styles.ratingValue}>{product.ratings}</span>
              <span className={styles.reviewCount}>
                ({product.reviews} reviews)
              </span>
            </div>
          </div>
        </div>

        <div className={styles.tabs}>
          {activeTab === "overview" && (
            <button
              className={`${styles.tab} ${
                activeTab === "overview" ? styles.activeTab : ""
              }`}
            >
              Overview
            </button>
          )}

          {activeTab === "notifications" && (
            <button
              className={`${styles.tab} ${
                activeTab === "notifications" ? styles.activeTab : ""
              }`}
            >
              Notifications
            </button>
          )}
        </div>

        {isConfigLoading ? (
          <Loading />
        ) : (
          <div className={styles.tabContent}>
            {activeTab === "overview" && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.3 }}
                className={styles.overviewTab}
              >
                <ProductDataChart product={product} />
              </motion.div>
            )}

            {activeTab === "notifications" && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.3 }}
                className={styles.notificationsTab}
              >
                <div className={styles.notificationOptions}>
                  <h4 className={styles.sectionTitle}>
                    Notification Preferences
                  </h4>
                  <div className={styles.notificationOption}>
                    <input
                      type="checkbox"
                      id="email-notification"
                      className={styles.checkbox}
                      checked={notificationType.includes("email")}
                      onChange={(e) =>
                        handleNotificationTypeChange("email", e.target.checked)
                      }
                    />
                    <label
                      htmlFor="email-notification"
                      className={styles.checkboxLabel}
                    >
                      Email Notifications
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
                      SMS Notifications
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
                      Whatsapp Notifications
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
                      Push Notifications
                    </label>
                  </div>
                </div>

                {!isStockAlertEnabled ? (
                  <>
                    <div className={styles.priceAlert}>
                      <h4 className={styles.sectionTitle}>Price Alert</h4>
                      <div className={styles.priceAlertOption}>
                        <input
                          type="radio"
                          id="automatic"
                          name="alert-type"
                          className={styles.radio}
                          value="automatic"
                          checked={alertType === "automatic"}
                          onChange={(e) => setAlertType(e.target.value)}
                        />
                        <label
                          htmlFor="automatic"
                          className={styles.radioLabel}
                        >
                          Automatic Price Drop Alert
                        </label>
                      </div>
                      <div
                        className={`${styles.priceAlertOption} ${styles.customPrice}`}
                      >
                        <div className={styles.customPriceLabel}>
                          <input
                            type="radio"
                            id="custom"
                            name="alert-type"
                            className={styles.radio}
                            value="custom"
                            checked={alertType === "custom"}
                            onChange={(e) => setAlertType(e.target.value)}
                          />
                          <label htmlFor="custom" className={styles.radioLabel}>
                            Custom Price Alert
                          </label>
                        </div>
                        {alertType === "custom" && (
                          <div className={styles.customPriceInputContainer}>
                            <span className={styles.currencySymbol}>
                              {getCurrencySymbol("INR")}
                            </span>
                            <input
                              type="number"
                              placeholder="0.00"
                              className={styles.customPriceInput}
                              value={customPrice}
                              onChange={(e) => setCustomPrice(e.target.value)}
                            />
                          </div>
                        )}
                      </div>
                    </div>

                    {isPincodeTrackingAvailable(product.store) && (
                      <div className={styles.pincodeAlert}>
                        <div
                          className={styles.pincodeAlertOption}
                          style={
                            receivePincodeUpdates
                              ? { marginBottom: "1rem" }
                              : { marginBottom: "0" }
                          }
                        >
                          <input
                            type="checkbox"
                            id="pincode-notification"
                            className={styles.checkbox}
                            checked={receivePincodeUpdates}
                            onChange={(e) =>
                              setReceivePincodeUpdates(e.target.checked)
                            }
                          />
                          <label
                            htmlFor="pincode-notification"
                            className={styles.checkboxLabel}
                          >
                            Pincode Availability Alert
                          </label>
                        </div>
                        {receivePincodeUpdates && (
                          <div className={styles.pincodeInputContainer}>
                            <input
                              type="text"
                              placeholder="123456"
                              className={styles.pincodeInput}
                              value={pincode}
                              onChange={(e) => setPincode(e.target.value)}
                            />
                          </div>
                        )}
                      </div>
                    )}

                    <div className={styles.notificationFrequency}>
                      <div
                        className={styles.notificationOption}
                        style={
                          receiveUpdates
                            ? { marginBottom: "1rem" }
                            : { marginBottom: "0" }
                        }
                      >
                        <input
                          type="checkbox"
                          id="notification-frequency"
                          className={styles.checkbox}
                          onChange={(e) => setReceiveUpdates(e.target.checked)}
                          checked={receiveUpdates}
                        />
                        <label
                          htmlFor="notification-frequency"
                          className={styles.checkboxLabel}
                        >
                          Notification Frequency
                        </label>
                      </div>

                      {receiveUpdates && (
                        <div className={styles.radioGroup}>
                          <div className={styles.radioOption}>
                            <input
                              type="radio"
                              id="weekly"
                              name="frequency"
                              value="WEEKLY"
                              className={styles.radio}
                              checked={updateFrequency === "WEEKLY"}
                              onChange={(e) =>
                                setUpdateFrequency(e.target.value)
                              }
                            />
                            <label
                              htmlFor="weekly"
                              className={styles.radioLabel}
                            >
                              Weekly
                            </label>
                          </div>
                          <div className={styles.radioOption}>
                            <input
                              type="radio"
                              id="monthly"
                              name="frequency"
                              value="MONTHLY"
                              className={styles.radio}
                              checked={updateFrequency === "MONTHLY"}
                              onChange={(e) =>
                                setUpdateFrequency(e.target.value)
                              }
                            />
                            <label
                              htmlFor="monthly"
                              className={styles.radioLabel}
                            >
                              Monthly
                            </label>
                          </div>
                          <div className={styles.radioOption}>
                            <input
                              type="radio"
                              id="custom-days"
                              name="frequency"
                              className={styles.radio}
                              value="CUSTOM"
                              checked={updateFrequency === "CUSTOM"}
                              onChange={(e) =>
                                setUpdateFrequency(e.target.value)
                              }
                            />
                            <label
                              htmlFor="custom-days"
                              className={styles.radioLabel}
                            >
                              Custom Days
                            </label>
                          </div>
                          {updateFrequency === "CUSTOM" && (
                            <div className={styles.pincodeInputContainer}>
                              <input
                                type="number"
                                placeholder="15"
                                min="1"
                                max="365"
                                className={styles.pincodeInput}
                                value={customNotificationDays}
                                onChange={(e) => setCustomNotificationDays(e.target.value)}
                              />
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </>
                ) : (
                  <div className={styles.stockAlert}>
                    <div className={styles.stockAlertOption}>
                      <input
                        type="radio"
                        id="stock-notification"
                        className={styles.checkbox}
                        checked={isStockAlertEnabled}
                        readOnly
                      />
                      <label
                        htmlFor="stock-notification"
                        className={styles.checkboxLabel}
                      >
                        <span style={{ fontWeight: "700" }}>Notify me</span>{" "}
                        when the product is back in stock
                      </label>
                    </div>
                  </div>
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

                <button
                  className={
                    !configSaved ? styles.saveButton : styles.successButton
                  }
                  onClick={handleConfigSave}
                  disabled={isConfigLoading}
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
                  {!configSaved
                    ? " Save Notification Settings"
                    : "Notification Settings Saved"}
                </button>
              </motion.div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default DashboardProductDetails;
