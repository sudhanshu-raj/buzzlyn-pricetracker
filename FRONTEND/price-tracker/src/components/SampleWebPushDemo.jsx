import React,{useState,useEffect} from "react";
import axios from "axios";
import usePushNotifications from "../hooks/usePushNotifications";
import style from "./SampleWebPushDemo.module.css";

const PushNotificationButton = () => {
  const { isSubscribed, subscribeFunction, unsubscribeFunction } = usePushNotifications();

  const handleButtonClick = () => {
    if (isSubscribed) {
      unsubscribeFunction();
    } else {
      subscribeFunction();
    }
  };

  return (
    <button className={style["custom-button"]} onClick={handleButtonClick}>
      {isSubscribed ? "Unsubscribe from Push Notifications" : "Subscribe to Push Notifications"}
    </button>
  );
};

const SendNotificationForm = () => {
  const [formData, setFormData] = useState({
    title: "",
    message: "",
    clickTarget: "",
    icon: "",
    image: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("http://localhost:8082/notifyAll", formData);
      alert("Notification sent successfully!");
      console.log(response.data);
    } catch (error) {
      console.error("Error sending notification:", error);
      alert("Failed to send notification.");
    }
  };

  return (
    <>
     <div className="App">
    <PushNotificationButton />
    <form className="notification-form" onSubmit={handleSubmit}>
      <h2 className="form-title">Send Notification</h2>
      <label className="form-label" htmlFor="title">Title:</label>
      <input
        className="form-input"
        type="text"
        id="title"
        name="title"
        value={formData.title}
        onChange={handleChange}
        required
      />

      <label className="form-label" htmlFor="clickTarget">Click Target (URL):</label>
      <input
        className="form-input"
        type="text"
        id="clickTarget"
        name="clickTarget"
        value={formData.clickTarget}
        onChange={handleChange}
        required
      />

      <label className="form-label" htmlFor="message">Message:</label>
      <textarea
        className="form-input form-textarea"
        id="message"
        name="message"
        value={formData.message}
        onChange={handleChange}
        required
      ></textarea>

      <label className="form-label" htmlFor="icon">Icon URL:</label>
      <input
        className="form-input"
        type="text"
        id="icon"
        name="icon"
        value={formData.icon}
        onChange={handleChange}
      />

      <button className="form-button" type="submit">Send Notification</button>
    </form>
    </div>
    </>
  );
};

export default SendNotificationForm;