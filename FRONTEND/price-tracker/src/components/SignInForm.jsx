"use client";

import { useState, useRef } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import styles from "./SignInForm.module.css";
import {
  isEmailExists,
  sendLoginOTP,
  otpLogin,
  passwordLogin,
  sendForgotPasswordOTP,
  verifyForgotPasswordOTP,
  resetPassword,
} from "../apicalls/authAPIs";
import { getErrorMessage } from "../services/handleErrorMssg";
import { ENDPOINTS } from "../apicalls/apiConfig";
import { useAuth } from "../context/AuthContext";

function SignInForm() {
  const navigate = useNavigate();
  const {setIsAuthenticated,setUser} = useAuth();

  const [isLoading, setIsLoading] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [authMethod, setAuthMethod] = useState(null); // null, "otp", or "password"
  const [otpSent, setOtpSent] = useState(false);
  const [otp, setOtp] = useState(["", "", "", ""]);
  const [error, setError] = useState("");
  const [forgotPasswordStep, setForgotPasswordStep] = useState(null); // null, "otp", "reset", "success"
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [forgetPasswToken, setForgetPasswToken] = useState(null);

  // Refs for OTP inputs
  const otpRefs = [useRef(null), useRef(null), useRef(null), useRef(null)];

  const handleGoogleSignUp = () => {
    window.location.href = ENDPOINTS.GOOGLE_OAUTH_URL;
  };

  const validateEmail = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const handleAuthOptionClick = async (method) => {
    // Reset previous errors
    setError("");

    // Check if email is provided
    if (!email) {
      setError("Please enter your email address");
      return;
    }
    // Validate email format
    if (!validateEmail(email)) {
      setError("Please enter a valid email address");
      return;
    }

    const CheckUserExistsRequest = {
      email: email,
    };

    const response = await isEmailExists(CheckUserExistsRequest);
    console.log("Response from checkEmail:", response);
    //here we checking response.success
    if (!response.success) {
      setError(getErrorMessage(response.error, "Email verification failed"));
      return;
    }

    // Set authentication method
    setAuthMethod(method);

    // If OTP method, send the code
    if (method === "otp") {
      sendOtp();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    if (authMethod === "otp") {
      // Handle OTP login
      const otpCode = otp.join("");
      if (otpCode.length !== 4) {
        setError("Please enter a valid 4-digit OTP code.");
        setIsLoading(false);
        return;
      }
      const OtpLoginRequest = {
        email: email,
        otp: otpCode,
      };
      const response = await otpLogin(OtpLoginRequest);
      if (!response.success) {
        setError(
          getErrorMessage(
            response.error,
            "Something went wrong. Please try again."
          )
        );
        setIsLoading(false);
        return;
      }

      //login completed successfully,now setting the user data in context
      let userData = null; 
      const rawUserData = response.data.user;
      if (rawUserData) {
        userData = {
          email: rawUserData.email,
          number: rawUserData.phoneNumber,
          firstName: rawUserData.firstName,
          avatarUrl: response.data.profilePic
            ? `data:image/png;base64,${response.data.profilePic}`
            : "/placeholder.svg?height=40&width=40",
        };
      }
      setUser(userData);
      setIsAuthenticated(true);

    } else if (authMethod === "password") {
      // Handle password login
      if (!password) {
        setError("Please enter your password.");
        setIsLoading(false);
        return;
      }
      const passwordLoginRequest = {
        email: email,
        password: password,
      };
      const response = await passwordLogin(passwordLoginRequest);
      if (!response.success) {
        setError(
          getErrorMessage(response.error, "Login failed,try again later.")
        );
        setIsLoading(false);
        return;
      }
       //login completed successfully,now setting the user data in context
      let userData = null; 
      const rawUserData = response.data.user;
      if (rawUserData) {
        userData = {
          email: rawUserData.email,
          number: rawUserData.phoneNumber,
          firstName: rawUserData.firstName,
          avatarUrl: response.data.profilePic
            ? `data:image/png;base64,${response.data.profilePic}`
            : "/placeholder.svg?height=40&width=40",
        };
      }
      setUser(userData);
      setIsAuthenticated(true);
    }
    //here verifying the forgot password otp ,if successfull then redirecting to reset password page
    else if (forgotPasswordStep && forgotPasswordStep === "otp") {
      const otpCode = otp.join("");
      if (otpCode.length !== 4) {
        setError("Please enter a valid 4-digit OTP code.");
        setIsLoading(false);
        return;
      }
      const OtpLoginRequest = {
        email: email,
        otp: otpCode,
      };
      const response = await verifyForgotPasswordOTP(OtpLoginRequest);
      if (!response.success) {
        setError(
          getErrorMessage(
            response.error,
            "Something went wrong. Please try again."
          )
        );
        setIsLoading(false);
        return;
      }
      // Successful OTP login would redirect to dashboard
      setForgetPasswToken(response.data);
      setForgotPasswordStep("reset");
    }

    setIsLoading(false);
  };

  const sendOtp = async (currentStep = null) => {
    setIsLoading(true);
    setError("");

    const step = currentStep || forgotPasswordStep;

    if (step === "otp") {
      const response = await sendForgotPasswordOTP(email);
      console.log("Response from sendForgotPasswordOTP:", response);
      if (!response.success) {
        setError(
          getErrorMessage(response.error, "Failed to send verification code")
        );
        setIsLoading(false);
        return;
      }
      setIsLoading(false);
      return;
    }

    const response = await sendLoginOTP(email);
    console.log("Response from sendLoginOTP:", response);
    if (!response.success) {
      setError(
        getErrorMessage(response.error, "Failed to send verification code")
      );
      setIsLoading(false);
      return;
    }
    //reach here if OTP is sent successfully
    setIsLoading(false);
    setOtpSent(true);
  };

  // Handle OTP input changes
  const handleOtpChange = (index, value) => {
    // Only allow digits
    if (!/^\d*$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    // Auto-focus next input
    if (value && index < 3) {
      otpRefs[index + 1].current.focus();
    }
  };

  // Handle backspace in OTP inputs
  const handleOtpKeyDown = (index, e) => {
    if (e.key === "Backspace") {
      if (!otp[index] && index > 0) {
        otpRefs[index - 1].current.focus();
      }
    }
  };

  // Handle paste for OTP
  const handleOtpPaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text");
    const digits = pastedData.replace(/\D/g, "").split("").slice(0, 4);

    const newOtp = [...otp];
    digits.forEach((digit, index) => {
      if (index < 4) newOtp[index] = digit;
    });
    setOtp(newOtp);

    // Focus the last filled input or the next empty one
    const lastIndex = Math.min(digits.length, 3);
    otpRefs[lastIndex].current.focus();
  };

  const handleForgotPassword = async () => {
    // Reset any previous errors
    setError("");

    // Check if email is provided
    if (!email) {
      setError("Please enter your email address");
      return;
    }
    // Validate email format
    if (!validateEmail(email)) {
      setError("Please enter a valid email address");
      return;
    }
    const CheckUserExistsRequest = {
      email: email,
    };
    // Check if email exists in the system
    const response = await isEmailExists(CheckUserExistsRequest);
    if (!response.success) {
      setError(getErrorMessage(response.error, "Email verification failed"));
      console.log("Error in checking email:", response.error);
      return;
    }

    // Start the forgot password flow
    setForgotPasswordStep("otp");
    sendOtp("otp");
  };

  // Add this function to handle password reset submission
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    // Validate password
    if (newPassword.length < 8) {
      setError("Password must be at least 8 characters long");
      setIsLoading(false);
      return;
    }

    // Validate password confirmation
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      setIsLoading(false);
      return;
    }

    // Prepare the request data
    const resetPasswordRequest = {
      email: email,
      password: newPassword,
    };
    const response = await resetPassword(
      resetPasswordRequest,
      forgetPasswToken
    );
    console.log("Response from resetPassword:", response);
    if (!response.success) {
      setError(getErrorMessage(response.error, "Password reset failed"));
      setIsLoading(false);
      return;
    }
    // Successful password reset would redirect to dashboard
    console.log("Password reset successful:", response.data);
    setIsLoading(false);
    setForgotPasswordStep("success");
  };

  // Add this function to handle returning to sign in after password reset
  const handleReturnToSignIn = () => {
    setForgotPasswordStep(null);
    setAuthMethod(null);
    setOtp(["", "", "", ""]);
    setNewPassword("");
    setConfirmPassword("");
    setError("");
  };

  const renderInitialView = () => (
    <>
      <h2 className={styles.formTitle}>Welcome back</h2>
      <p className={styles.formSubtitle}>Sign in to your account to continue</p>

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
          Sign in with Google
        </button>
      </div>

      <div className={styles.divider}>
        <span className={styles.dividerText}>or continue with email</span>
      </div>

      <form className={styles.form}>
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
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className={styles.authOptions}>
          <button
            type="button"
            className={styles.otpAuthOptionButton}
            onClick={() => handleAuthOptionClick("otp")}
          >
            Login with OTP
          </button>
          <button
            type="button"
            className={styles.passwordAuthOptionButton}
            onClick={() => handleAuthOptionClick("password")}
          >
            Login with Password
          </button>
        </div>

        {error && <div className={styles.errorMessage}>{error}</div>}
      </form>

      <div className={styles.helpLink}>
        <a
          href="#"
          className={styles.forgotPassword}
          onClick={handleForgotPassword}
        >
          Need Password Help?
        </a>
      </div>
    </>
  );

  const renderOtpView = () => (
    <>
      <button
        className={styles.backButton}
        onClick={() => {
          if (forgotPasswordStep) {
            setForgotPasswordStep(null);
          } else {
            setAuthMethod(null);
          }
          setOtpSent(false);
          setOtp(["", "", "", ""]);
          setError("");
        }}
      >
        ← Back
      </button>

      <h2 className={styles.formTitle}>
        {forgotPasswordStep ? "Reset Your Password" : "Enter verification code"}
      </h2>
      <p className={styles.formSubtitle}>
        We've sent a 4-digit code to <strong>{email}</strong>
      </p>

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.otpContainer}>
          {otp.map((digit, index) => (
            <input
              key={`otp-${index}`}
              ref={otpRefs[index]}
              type="text"
              maxLength={1}
              className={styles.otpInput}
              value={digit}
              onChange={(e) => handleOtpChange(index, e.target.value)}
              onKeyDown={(e) => handleOtpKeyDown(index, e)}
              onPaste={handleOtpPaste}
              disabled={isLoading}
              required
            />
          ))}
        </div>

        {error && <div className={styles.errorMessage}>{error}</div>}

        <div className={styles.otpActions}>
          <button
            type="button"
            className={styles.resendButton}
            onClick={sendOtp}
            disabled={isLoading}
          >
            Resend Code
          </button>
          <button
            type="submit"
            className={styles.submitButton}
            disabled={isLoading || otp.some((digit) => !digit)}
          >
            {isLoading
              ? "Verifying..."
              : forgotPasswordStep
              ? "Continue"
              : "Sign In"}
          </button>
        </div>
      </form>
    </>
  );

  const renderPasswordView = () => (
    <>
      <button
        className={styles.backButton}
        onClick={() => {
          setAuthMethod(null);
          setPassword("");
          setError("");
        }}
      >
        ← Back
      </button>

      <h2 className={styles.formTitle}>Enter your password</h2>
      <p className={styles.formSubtitle}>
        Sign in to <strong>{email}</strong>
      </p>

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label htmlFor="password" className={styles.label}>
            Password
          </label>
          <input
            id="password"
            type="password"
            className={styles.input}
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isLoading}
            required
          />
        </div>

        {error && <div className={styles.errorMessage}>{error}</div>}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={isLoading}
        >
          {isLoading ? "Signing in..." : "Sign In"}
        </button>
      </form>

      <div className={styles.helpLink}>
        <a
          href="#"
          className={styles.forgotPassword}
          onClick={handleForgotPassword}
        >
          Need Password Help?
        </a>
      </div>
    </>
  );

  const renderResetPasswordView = () => (
    <>
      <button
        className={styles.backButton}
        onClick={() => {
          setForgotPasswordStep("otp");
          setError("");
        }}
      >
        ← Back
      </button>

      <h2 className={styles.formTitle}>Create New Password</h2>
      <p className={styles.formSubtitle}>
        Please enter a new password for <strong>{email}</strong>
      </p>

      <form className={styles.form} onSubmit={handleResetPassword}>
        <div className={styles.formGroup}>
          <label htmlFor="newPassword" className={styles.label}>
            New Password
          </label>
          <input
            id="newPassword"
            type="password"
            className={styles.input}
            placeholder="••••••••"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            disabled={isLoading}
            required
            minLength={8}
          />
          <p className={styles.passwordHint}>
            Password must be at least 8 characters long
          </p>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="confirmPassword" className={styles.label}>
            Confirm Password
          </label>
          <input
            id="confirmPassword"
            type="password"
            className={styles.input}
            placeholder="••••••••"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isLoading}
            required
          />
        </div>

        {error && <div className={styles.errorMessage}>{error}</div>}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={isLoading}
        >
          {isLoading ? "Resetting Password..." : "Reset Password"}
        </button>
      </form>
    </>
  );

  const renderPasswordResetSuccessView = () => (
    <div className={styles.successContainer}>
      <div className={styles.successIcon}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="48"
          height="48"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
      </div>
      <h2 className={styles.successTitle}>Password Reset Successful</h2>
      <p className={styles.successText}>
        Your password has been reset successfully. You can now sign in with your
        new password.
      </p>
      <button className={styles.submitButton} onClick={handleReturnToSignIn}>
        Sign In
      </button>
    </div>
  );

  const renderContent = () => {
    if (forgotPasswordStep === "otp") {
      return renderOtpView();
    } else if (forgotPasswordStep === "reset") {
      return renderResetPasswordView();
    } else if (forgotPasswordStep === "success") {
      return renderPasswordResetSuccessView();
    } else if (authMethod === "otp") {
      return renderOtpView();
    } else if (authMethod === "password") {
      return renderPasswordView();
    } else {
      return renderInitialView();
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 20 }}
      transition={{ duration: 0.3 }}
    >
      {renderContent()}
    </motion.div>
  );
}

export default SignInForm;
