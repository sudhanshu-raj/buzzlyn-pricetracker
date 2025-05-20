import { useState, useEffect, useRef } from "react";
import { motion } from "framer-motion";
import styles from "./SignUpForm.module.css";
import DOMPurify from "dompurify";
import {
  checkEmail,
  checkPhone,
  sendSignUpOTP,
  verifySignUpOTP,
  registerFormUser,
  checkUserOtpVrfd,
  registerOAuthUser,
} from "../apicalls/authAPIs";
import { ENDPOINTS } from "../apicalls/apiConfig";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

function SignUpForm({ onToggleForm, oauthEmail = "", isOAuthUser = false }) {

  const {setIsAuthenticated,setUser} = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phoneNumber: "",
    countryCode: "+1",
    agreeToTerms: false,
  });
  const [isLoading, setIsLoading] = useState(false);
  const [emailOtpSent, setEmailOtpSent] = useState(false);
  const [phoneOtpSent, setPhoneOtpSent] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [phoneVerified, setPhoneVerified] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [registerError, setRegisterError] = useState("");

  // Timer for OTP resend
  const [emailOtpTimer, setEmailOtpTimer] = useState(30);
  const [emailResendDisabled, setEmailResendDisabled] = useState(false);
  const [phoneOtpTimer, setPhoneOtpTimer] = useState(30);
  const [phoneResendDisabled, setPhoneResendDisabled] = useState(false);

  //OTP responses
  const [sendEmailOtpResponse, setSendEmailOtpResponse] = useState(null);
  const [sendPhoneOtpResponse, setSendPhoneOtpResponse] = useState(null);

  // OTP state
  const [emailOtp, setEmailOtp] = useState(["", "", "", ""]);
  const [phoneOtp, setPhoneOtp] = useState(["", "", "", ""]);

  //OTP errors
  const [emailOtpError, setEmailOtpError] = useState("");
  const [phoneOtpError, setPhoneOtpError] = useState("");
  const [phoneOtpSendingError, setPhoneOtpSendingError] = useState(null);

  // Refs for OTP inputs
  const emailOtpRefs = [useRef(null), useRef(null), useRef(null), useRef(null)];
  const phoneOtpRefs = [useRef(null), useRef(null), useRef(null), useRef(null)];
  const [isRegisterSuccessful, setIsRegisterSuccessful] = useState(false);

  // Country codes for dropdown
  const countryCodes = [
    { code: "+1", country: "US" },
    { code: "+44", country: "UK" },
    { code: "+91", country: "IN" },
    { code: "+61", country: "AU" },
    { code: "+86", country: "CN" },
    { code: "+49", country: "DE" },
    { code: "+33", country: "FR" },
    { code: "+81", country: "JP" },
  ];

  const handleGoogleSignUp = () => {
    window.location.href = ENDPOINTS.GOOGLE_OAUTH_URL;
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  //here remember we are not saving data here, just verifying the email if the email is already registered or not
  const handleSubmitBasicInfo = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setRegisterError(null);

    console.log("Form Data before sanitization:", formData);
    const CheckUserExistsRequest = {
      email: formData.email,
    };

    const response = await checkEmail(CheckUserExistsRequest);
    console.log("Response from checkEmail:", response);
    if (response.success == false) {
      setRegisterError(response.error);
      setIsLoading(false);
      return;
    }
    //reached here means email is not registered, so we can proceed with the signup process
    setIsLoading(false);
    setStep(2);
    // Check if the user needs to verify their email
    const isUsrOtpVrfd = await checkUsrOtpVrfd(formData.email);
    if (isUsrOtpVrfd) {
      setEmailVerified(true);
      return;
    }
    sendEmailOtp();
  };

  /**
   *
   * @param {string} userId
   * @returns {boolean} - Returns true if the user does not need verification, false otherwise
   */
  const checkUsrOtpVrfd = async (userId) => {
    try {
      const response = await checkUserOtpVrfd(userId);
      console.log("Response from checkUsrOtpVrfd:", response);
      if (response.success == false) {
        return false;
      }
      if (
        response.data != null &&
        response.data.isUserNeedVerification == true
      ) {
        return false;
      }
      return true;
    } catch (error) {
      console.error("Error checking OTP verification:", error);
      return false;
    }
  };

  const sendEmailOtp = async () => {
    setIsLoading(true);
    setEmailOtpError(null);
    setEmailResendDisabled(true);
    setEmailOtp(["", "", "", ""]);
    setEmailOtpSent(false);

    var OtpRequest;
    if (sendEmailOtpResponse != null) {
      OtpRequest = {
        otpId: sendEmailOtpResponse.pt_ky,
        isEmail: "true",
        userId: formData.email,
      };
    } else {
      OtpRequest = {
        userId: formData.email,
        isEmail: "true",
      };
    }
    const response = await sendSignUpOTP(OtpRequest);
    console.log("Response from sendSignUpOTP:", response);
    if (response.success == false) {
      setEmailOtpError(response.error);
      setIsLoading(false);
      setEmailResendDisabled(false);
      return;
    }

    //reached here means otp sent, now we can proceed with the next step
    setSendEmailOtpResponse(response.data);
    setIsLoading(false);
    setEmailOtpSent(true);
  };

  const sendPhoneOtp = async () => {
    setIsLoading(true);
    setIsEditing(false);
    setPhoneOtpError(null);
    setPhoneResendDisabled(true);
    setPhoneOtp(["", "", "", ""]);
    setPhoneOtpSent(false);
    setPhoneOtpSendingError(null);

    const phoneNumber = formData.countryCode + formData.phoneNumber;
    const isUsrOtpVrfd = await checkUsrOtpVrfd(phoneNumber);
    if (isUsrOtpVrfd) {
      setPhoneVerified(true);
      setIsLoading(false);
      return;
    }

    var CheckUserExistsRequest = {
      phoneNumber: phoneNumber,
    };
    console.log("CheckUserExistsRequest:", CheckUserExistsRequest);
    const response = await checkPhone(CheckUserExistsRequest);
    console.log("Response from checkPhone:", response);

    if (response.success == false) {
      setPhoneOtpSendingError(response.error);
      setIsLoading(false);
      return;
    }

    var OtpRequest;

    if (sendPhoneOtpResponse != null) {
      OtpRequest = {
        otpId: sendPhoneOtpResponse.pt_ky,
        isEmail: "false",
        userId: phoneNumber,
      };
    } else {
      OtpRequest = {
        userId: phoneNumber,
        isEmail: "false",
      };
    }

    const otpResponse = await sendSignUpOTP(OtpRequest);
    console.log("Response from sendSignUpOTP:", otpResponse);

    if (otpResponse.success == false) {
      setPhoneOtpSendingError(otpResponse.error);
      setIsLoading(false);
      setPhoneResendDisabled(false);
      return;
    }
    //reached here means otp sent, now we can proceed with the next step
    setSendPhoneOtpResponse(otpResponse.data);
    setPhoneOtpSent(true);
    setIsLoading(false);
  };

  const verifyEmailOtp = async () => {
    setIsLoading(true);
    setEmailOtpError(null);
    const enteredOtp = emailOtp.join("");

    if (sendEmailOtpResponse == null || sendEmailOtpResponse.pt_ky == null) {
      setRegisterError("Invalid OTP ");
      setIsLoading(false);
      return;
    }

    const VerifyOtpRequest = {
      pt_ky: sendEmailOtpResponse.pt_ky,
      otp: enteredOtp,
      isEmail: "true",
    };

    const response = await verifySignUpOTP(VerifyOtpRequest);
    console.log("Response from verifySignUpOTP:", response);
    if (response.success == false) {
      setEmailOtpError(response.error);
      setIsLoading(false);
      return;
    }
    //reached here means otp is verified, now we can proceed with the next step
    setIsLoading(false);
    setEmailVerified(true);
  };

  const verifyPhoneOtp = async () => {
    setIsLoading(true);
    setPhoneOtpError(null);
    const enteredOtp = phoneOtp.join("");

    if (sendPhoneOtpResponse == null || sendPhoneOtpResponse.pt_ky == null) {
      setRegisterError("Invalid OTP ");
      setIsLoading(false);
      return;
    }

    const VerifyOtpRequest = {
      pt_ky: sendPhoneOtpResponse.pt_ky,
      otp: enteredOtp,
      isEmail: "false",
    };

    const response = await verifySignUpOTP(VerifyOtpRequest);
    console.log("Response from verifySignUpOTP:", response);
    if (response.success == false) {
      setPhoneOtpError(response.error);
      setIsLoading(false);
      return;
    }
    //reached here means otp is verified, now we can proceed with the next step
    setIsLoading(false);
    setPhoneVerified(true);
  };

  const completeSignup = async () => {
    setIsLoading(true);
    setRegisterError(null);

    if (isOAuthUser) {
      const RegisterRequest = {
        email: oauthEmail,
        phoneNumber: formData.countryCode + formData.phoneNumber,
        pt_ky: sendPhoneOtpResponse.pt_ky,
      };
      const response = await registerOAuthUser(RegisterRequest);
      console.log("Response from registerOAuthUser:", response);
      if (response.success == false) {
        setRegisterError(response.error);
        setIsLoading(false);
        return;
      }
      //reached here means registration is successful, now we can proceed with the next step
      let userData = null;
      const rawUserData = response.data.user;
      if (rawUserData) {
        userData = {
          email: rawUserData.email ? rawUserData.email : oauthEmail,
          number: rawUserData.phoneNumber ? rawUserData.phoneNumber : "",
          firstName: rawUserData.firstName ? rawUserData.firstName : "",
          avatarUrl: response.data.profilePic
            ? `data:image/png;base64,${response.data.profilePic}`
            : "/placeholder.svg?height=40&width=40",
        };
      }

      setIsRegisterSuccessful(true);
      setIsLoading(false);
      setStep(3);

      setTimeout(() => {
        if (userData) {  // Add safety check
          setUser(userData);
        }
        setIsAuthenticated(true);
      }, 2000); // 2 seconds delay

      return;
    }

    // If not an OAuth user, proceed with regular registration
    const phoneNumber = formData.countryCode + formData.phoneNumber;
    const RegisterRequest = {
      firstName: formData.firstName,
      lastName: formData.lastName,
      email: formData.email,
      password: formData.password,
      phoneNumber: phoneNumber,
      email_pt_ky: sendEmailOtpResponse.pt_ky,
      phone_pt_ky: sendPhoneOtpResponse.pt_ky,
    };
    console.log("RegisterRequest:", RegisterRequest);
    const response = await registerFormUser(RegisterRequest);
    console.log("Response from registerFormUser:", response);
    if (response.success == false) {
      setRegisterError(response.error);
      setIsLoading(false);
      return;
    }
    //reached here means registration is successful, now we can proceed with the next step
    let userData = null; 
    const rawUserData = response.data.user;
    const profilePicBase64 = response.data.profilePic
    ? `data:image/png;base64,${response.data.profilePic}`
    : "/placeholder.svg?height=40&width=40";
    if (rawUserData) {
      userData = {
        email: rawUserData.email,
        number:rawUserData.phoneNumber,
        firstName: rawUserData.firstName,
        avatarUrl:profilePicBase64,
      };
    }

    setIsRegisterSuccessful(true);
    setIsLoading(false);
    setStep(3);

    setTimeout(() => {
      if (userData) {  // Add safety check
        setUser(userData);
      }
      setIsAuthenticated(true); // This will trigger redirect after delay
    }, 2000); // 2 seconds delay
  };

  // Handle OTP input changes
  const handleOtpChange = (index, value, type) => {
    // Only allow digits
    if (!/^\d*$/.test(value)) return;

    if (type === "email") {
      const newOtp = [...emailOtp];
      newOtp[index] = value;
      setEmailOtp(newOtp);

      // Auto-focus next input
      if (value && index < 3) {
        emailOtpRefs[index + 1].current.focus();
      }
    } else {
      const newOtp = [...phoneOtp];
      newOtp[index] = value;
      setPhoneOtp(newOtp);

      // Auto-focus next input
      if (value && index < 3) {
        phoneOtpRefs[index + 1].current.focus();
      }
    }
  };

  // Handle backspace in OTP inputs
  const handleOtpKeyDown = (index, e, type) => {
    if (e.key === "Backspace") {
      if (type === "email") {
        if (!emailOtp[index] && index > 0) {
          emailOtpRefs[index - 1].current.focus();
        }
      } else {
        if (!phoneOtp[index] && index > 0) {
          phoneOtpRefs[index - 1].current.focus();
        }
      }
    }
  };

  // Handle paste for OTP
  const handleOtpPaste = (e, type) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text");
    const digits = pastedData.replace(/\D/g, "").split("").slice(0, 4);

    if (type === "email") {
      const newOtp = [...emailOtp];
      digits.forEach((digit, index) => {
        if (index < 4) newOtp[index] = digit;
      });
      setEmailOtp(newOtp);

      // Focus the last filled input or the next empty one
      const lastIndex = Math.min(digits.length, 3);
      emailOtpRefs[lastIndex].current.focus();
    } else {
      const newOtp = [...phoneOtp];
      digits.forEach((digit, index) => {
        if (index < 4) newOtp[index] = digit;
      });
      setPhoneOtp(newOtp);

      // Focus the last filled input or the next empty one
      const lastIndex = Math.min(digits.length, 3);
      phoneOtpRefs[lastIndex].current.focus();
    }
  };

  // If this is an OAuth user, skips all and just verify the phone number
  useEffect(() => {
    if (isOAuthUser && oauthEmail) {
      setStep(2);
      setEmailVerified(true);
    }
  }, [isOAuthUser, oauthEmail]);

  // Watch for phone number changes to automatically send OTP
  useEffect(() => {
    // Check if phone number is 10 digits (without country code)
    if (
      formData.phoneNumber.replace(/\D/g, "").length === 10 &&
      !phoneOtpSent
    ) {
      sendPhoneOtp();
    }
  }, [formData.phoneNumber]);

  useEffect(() => {
    let interval;

    if (emailResendDisabled && emailOtpTimer > 0) {
      interval = setInterval(() => {
        setEmailOtpTimer((prevTimer) => prevTimer - 1);
      }, 1000);
    } else if (emailOtpTimer === 0) {
      setEmailResendDisabled(false);
      setEmailOtpTimer(30);
    }

    return () => clearInterval(interval);
  }, [emailResendDisabled, emailOtpTimer]);

  useEffect(() => {
    let interval;

    if (phoneResendDisabled && phoneOtpTimer > 0) {
      interval = setInterval(() => {
        setPhoneOtpTimer((prevTimer) => prevTimer - 1);
      }, 1000);
    } else if (phoneOtpTimer === 0) {
      setPhoneResendDisabled(false);
      setPhoneOtpTimer(30);
    }

    return () => clearInterval(interval);
  }, [phoneResendDisabled, phoneOtpTimer]);

  // Automatically complete signup if both email and phone are verified
  useEffect(() => {
    if (emailVerified && phoneVerified) {
      const timer = setTimeout(completeSignup, 1000); // 2 seconds

      if (isRegisterSuccessful) {
        navigate("/track")
      }

      return () => clearTimeout(timer); // Cleanup function to clear the timeout
    }
  }, [emailVerified, phoneVerified]);

  const encoded = "&lt;script&gt; alert(&#34;attack&#34;)&lt;/script&gt;";

  const renderStepOne = () => (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      transition={{ duration: 0.3 }}
    >
      <h2 className={styles.formTitle}>Create an account</h2>
      <p className={styles.formSubtitle}>Sign up to start tracking prices</p>

      <div className={styles.socialButtons}>
        <button className={styles.socialButton} onClick={handleGoogleSignUp}>
          <svg className={styles.socialIcon} viewBox="0 0 24 24">
            <path
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              fill="#4285F4"
            />
            <path
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              fill="#34A853"
            />
            <path
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              fill="#FBBC05"
            />
            <path
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              fill="#EA4335"
            />
          </svg>
          Sign up with Google
        </button>
      </div>

      <div className={styles.divider}>
        <span className={styles.dividerText}>or continue with email</span>
      </div>

      <form className={styles.form} onSubmit={handleSubmitBasicInfo}>
        <div className={styles.formRow}>
          <div className={styles.formGroup}>
            <label htmlFor="firstName" className={styles.label}>
              First Name
            </label>
            <input
              id="firstName"
              name="firstName"
              type="text"
              className={styles.input}
              placeholder="John"
              required
              value={formData.firstName}
              onChange={handleChange}
            />
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="lastName" className={styles.label}>
              Last Name
            </label>
            <input
              id="lastName"
              name="lastName"
              type="text"
              className={styles.lastNameInput}
              placeholder="Doe"
              required
              value={formData.lastName}
              onChange={handleChange}
            />
          </div>
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="email" className={styles.label}>
            Email
          </label>
          <input
            id="email"
            name="email"
            type="email"
            className={styles.input}
            placeholder="name@example.com"
            required
            value={formData.email}
            onChange={handleChange}
          />
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="password" className={styles.label}>
            Password
          </label>
          <input
            id="password"
            name="password"
            type="password"
            className={styles.input}
            placeholder="••••••••"
            required
            value={formData.password}
            onChange={handleChange}
            minLength={8}
          />
        </div>
        <div className={styles.registerError}>
          <p className={styles.errorText}>{registerError}</p>
        </div>
        <div className={styles.formGroup}>
          <div className={styles.checkbox}>
            <input
              type="checkbox"
              id="agreeToTerms"
              name="agreeToTerms"
              required
              checked={formData.agreeToTerms}
              onChange={handleChange}
            />
            <label htmlFor="agreeToTerms" className={styles.checkboxLabel}>
              I agree to the{" "}
              <a href="#" className={styles.link}>
                Terms of Service
              </a>{" "}
              and{" "}
              <a href="#" className={styles.link}>
                Privacy Policy
              </a>
            </label>
          </div>
        </div>
        <div className={styles.formGroup}>
          <button
            type="submit"
            className={styles.submitButton}
            disabled={isLoading}
          >
            {isLoading ? "Processing..." : "Continue"}
          </button>
        </div>
      </form>
    </motion.div>
  );

  const renderStepTwo = () => (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      transition={{ duration: 0.3 }}
    >
      <h2 className={styles.formTitle}>Verify Your Account</h2>
      <p className={styles.formSubtitle}>
        We need to verify your email and phone number
      </p>

      {/* Email Verification Section */}
      <div
        className={`${styles.verificationSection} ${
          emailVerified ? styles.verified : ""
        }`}
      >
        <div className={styles.verificationHeader}>
          <h3 className={styles.verificationTitle}>
            <span className={styles.verificationNumber}>1</span> Email
            Verification
            {emailVerified && (
              <span className={styles.verifiedBadge}>
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
                >
                  <path d="M20 6L9 17l-5-5" />
                </svg>
                Verified
              </span>
            )}
          </h3>
        </div>

        {!emailVerified && (
          <>
            <p className={styles.verificationSubtitle}>
              We've sent a 4-digit code to <strong>{formData.email}</strong>
            </p>
            <div className={styles.otpContainer}>
              {emailOtp.map((digit, index) => (
                <input
                  key={`email-otp-${index}`}
                  ref={emailOtpRefs[index]}
                  type="text"
                  maxLength={1}
                  className={styles.otpInput}
                  value={digit}
                  onChange={(e) =>
                    handleOtpChange(index, e.target.value, "email")
                  }
                  onKeyDown={(e) => handleOtpKeyDown(index, e, "email")}
                  onPaste={(e) => handleOtpPaste(e, "email")}
                  disabled={isLoading}
                />
              ))}
            </div>
            <div className={styles.registerError}>
              <p className={styles.errorTextOTP}>{emailOtpError}</p>
            </div>
            <div className={styles.otpActions}>
              <button
                type="button"
                className={styles.resendButton}
                onClick={sendEmailOtp}
                disabled={isLoading || emailResendDisabled}
              >
                {emailResendDisabled
                  ? `Resend in (${emailOtpTimer}s)`
                  : "Resend Code"}
              </button>
              <button
                type="button"
                className={styles.verifyButton}
                onClick={verifyEmailOtp}
                disabled={isLoading || emailOtp.some((digit) => !digit)}
              >
                {isLoading ? "Verifying..." : "Verify Email"}
              </button>
            </div>
          </>
        )}
      </div>

      {/* Phone Verification Section */}
      <div
        className={`${styles.verificationSection} ${
          phoneVerified ? styles.verified : ""
        }`}
      >
        <div className={styles.verificationHeader}>
          <h3 className={styles.verificationTitle}>
            <span className={styles.verificationNumber}>2</span> Phone
            Verification
            {phoneVerified && (
              <span className={styles.verifiedBadge}>
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
                >
                  <path d="M20 6L9 17l-5-5" />
                </svg>
                Verified
              </span>
            )}
          </h3>
        </div>

        {!phoneVerified ? (
          <>
            <div className={styles.phoneInputGroup}>
              <select
                name="countryCode"
                className={styles.countryCodeSelect}
                value={formData.countryCode}
                onChange={handleChange}
                disabled={phoneOtpSent && !isEditing}
              >
                {countryCodes.map((country) => (
                  <option key={country.code} value={country.code}>
                    {country.code} ({country.country})
                  </option>
                ))}
              </select>
              <input
                type="tel"
                maxLength={10}
                name="phoneNumber"
                className={styles.phoneInput}
                placeholder="Phone number"
                value={formData.phoneNumber}
                onChange={handleChange}
                disabled={phoneOtpSent && !isEditing}
                required
              />
              {phoneOtpSent && !isEditing && (
                <button
                  type="button"
                  className={styles.editButton}
                  onClick={() => (setIsEditing(true), setPhoneOtpSent(false))}
                >
                  Edit
                </button>
              )}
            </div>

            <div className={styles.registerError}>
              <p className={styles.errorTextOTP}>{phoneOtpSendingError}</p>
            </div>

            {phoneOtpSent && (
              <>
                <p className={styles.otpSentMessage}>
                  We've sent a 4-digit code to {formData.countryCode}{" "}
                  {formData.phoneNumber}
                </p>
                <div className={styles.otpContainer}>
                  {phoneOtp.map((digit, index) => (
                    <input
                      key={`phone-otp-${index}`}
                      ref={phoneOtpRefs[index]}
                      type="text"
                      maxLength={1}
                      className={styles.otpInput}
                      value={digit}
                      onChange={(e) =>
                        handleOtpChange(index, e.target.value, "phone")
                      }
                      onKeyDown={(e) => handleOtpKeyDown(index, e, "phone")}
                      onPaste={(e) => handleOtpPaste(e, "phone")}
                      disabled={isLoading}
                    />
                  ))}
                </div>
                <div className={styles.registerError}>
                  <p className={styles.errorTextOTP}>{phoneOtpError}</p>
                </div>

                <div className={styles.otpActions}>
                  <button
                    type="button"
                    className={styles.resendButton}
                    onClick={sendPhoneOtp}
                    disabled={isLoading || phoneResendDisabled}
                  >
                    {phoneResendDisabled
                      ? `Resend in (${phoneOtpTimer}s)`
                      : "Resend Code"}
                  </button>
                  <button
                    type="button"
                    className={styles.verifyButton}
                    onClick={verifyPhoneOtp}
                    disabled={isLoading || phoneOtp.some((digit) => !digit)}
                  >
                    {isLoading ? "Verifying..." : "Verify Phone"}
                  </button>
                </div>
              </>
            )}
          </>
        ) : null}
      </div>

      {/* Complete Registration Button */}
    </motion.div>
  );

  const renderStepThree = () => (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{ duration: 0.3 }}
      className={styles.successStep}
    >
      <div className={styles.successIcon}>
        <motion.svg
          xmlns="http://www.w3.org/2000/svg"
          width="48"
          height="48"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          initial={{ strokeDasharray: 100, strokeDashoffset: 100 }}
          animate={{ strokeDashoffset: 0 }}
          transition={{ duration: 1 }}
        >
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </motion.svg>
      </div>
      <h2 className={styles.successTitle}>Registration Complete!</h2>

      <motion.div
        className={styles.redirectContainer}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5, duration: 0.5 }}
      >
        {/* <p className={styles.redirectText}>Redirecting to sign in page</p> */}
        <motion.div
          className={styles.redirectAnimation}
          animate={{
            x: [0, 10, 0],
            transition: {
              repeat: Infinity,
              duration: 1.5,
              ease: "easeInOut",
            },
          }}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
          </svg>
        </motion.div>
      </motion.div>
    </motion.div>
  );

  const renderCurrentStep = () => {
    switch (step) {
      case 1:
        return renderStepOne();
      case 2:
        return renderStepTwo();
      case 3:
        return renderStepThree();
      default:
        return renderStepOne();
    }
  };

  return (
    <div className={styles.signupFormContainer}>
      {step < 3 && (
        <div className={styles.progressBar}>
          <div
            className={styles.progressFill}
            style={{ width: `${(step / 2) * 100}%` }}
          ></div>
        </div>
      )}
      {renderCurrentStep()}
    </div>
  );
}

export default SignUpForm;
