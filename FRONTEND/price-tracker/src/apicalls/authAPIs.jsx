import axios from "axios";
import { ENDPOINTS } from "./apiConfig";

/**
 *
 * @param {object} OtpRequest
 * @param {string} OtpRequest.userId - The email address of the user ,becomes option if re-sending OTP
 * @param {string} OtpRequest.isEmail - The OTP type (email or phone)
 * @param {string} OtpRequest.otpId - The OTP ID (optional),but rquired for resending OTP
 * @returns {Promise} - Response from the API
 */
const sendSignUpOTP = async (OtpRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.SIGNUP_OTP_SEND_URL,
      OtpRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error sending OTP:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to send OTP",
    };
  }
};

/**
 *
 * @param {object} VerifyOtpRequest
 * @param {string} VerifyOtpRequest.pt_ky - Identifier for the OTP
 * @param {string} VerifyOtpRequest.otp - The OTP code entered by the user
 * @param {string} VerifyOtpRequest.isEmail - The OTP type (email or phone)
 * @returns {Promise} - Response from the API
 */
const verifySignUpOTP = async (VerifyOtpRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.SIGNUP_OTP_VERIFY_URL,
      VerifyOtpRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error verifying OTP:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to verify OTP",
    };
  }
};

/**
 *
 * @param {object} OAuthUserRequest
 * @param {string} OAuthUserRequest.email - The email address of the user
 * @param {string} OAuthUserRequest.phoneNumber - The phone number of the user
 * @param {string} OAuthUserRequest.pt_ky - The OTP ID
 * @returns {Promise} - Response from the API
 */
const registerOAuthUser = async (OAuthUserRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.REGISTER_OAUTH_URL,
      OAuthUserRequest,
      { withCredentials: true } 
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error creating new oauth user:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to register user",
    };
  }
};

/**
 *
 * @param {object} CheckUserRequest
 * @param {string} CheckUserRequest.firstName - The first name of the user
 * @param {string} CheckUserRequest.lastName - The last name of the user
 * @param {string} CheckUserRequest.phoneNumber - The phone number of the user
 * @param {string} CheckUserRequest.email - The email address of the user
 * @param {string} CheckUserRequest.password - The password of the user for testing purposes
 * @returns  {Promise} - Response from the API
 * @description This function checks if the user is already registered or not.
 */
const preFormRegisterCheck = async (CheckUserRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.PREREGISTER_CHECK_URL,
      CheckUserRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error checking pre-form registration:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to verify email",
    };
  }
};

/**
 *
 * @param {object} FormUserRequest
 * @param {string} FormUserRequest.firstName - The first name of the user
 * @param {string} FormUserRequest.lastName - The last name of the user
 * @param {string} FormUserRequest.phoneNumber - The phone number of the user
 * @param {string} FormUserRequest.email - The email address of the user
 * @param {string} FormUserRequest.email_pt_ky - The email OTP ID
 * @param {string} FormUserRequest.phone_pt_ky - The phone OTP ID
 * @param {string} FormUserRequest.password - The password of the user
 *
 * @returns
 */
const registerFormUser = async (FormUserRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.REGISTER_FORMUSER_URL,
      FormUserRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error registering form user:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to register user",
    };
  }
};

/**
 *
 * @param {object} CheckUserExistsRequest
 * @param {string} CheckUserExistsRequest.email - The email address of the user
 * @returns {Promise} - Response from the API
 */
const checkEmail = async (CheckUserExistsRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.CHECK_EMAIL_URL,
      CheckUserExistsRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error checking email:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to verify email",
    };
  }
};

/**
 *
 * @param {object} CheckUserExistsRequest
 * @param {string} CheckUserExistsRequest.email - The email address of the user
 * @returns {Promise} - Response from the API
 */
const isEmailExists = async (CheckUserExistsRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.ISEMAIL_EXISTS_URL,
      CheckUserExistsRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error checking email:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to verify email",
    };
  }
};

/**
 *
 * @param {object} CheckUserExistsRequest
 * @param {string} CheckUserExistsRequest.phoneNumber - The phone number of the user
 * @returns
 */
const checkPhone = async (CheckUserExistsRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.CHECK_PHONE_URL,
      CheckUserExistsRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error checking phone:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to verify phone",
    };
  }
};

/**
 *
 * @param {String} userId - The user ID to check
 * @returns  {Promise} - Response from the API
 */

const checkUserOtpVrfd = async (userId) => {
  try {
    const response = await axios.post(
      `${ENDPOINTS.OTP_VERIFICATION_NEEDED_URL}?userId=${userId}`
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error checking checkUserOtpVrfd:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to process request",
    };
  }
};

const sendLoginOTP = async (email) => {
  try {
    const response = await axios.post(
      `${ENDPOINTS.LOGINOTP_SEND_URL}?email=${email}`
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error sending login OTP:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to send OTP",
    };
  }
};
/**
 *
 * @param {object} OtpLoginRequest
 * @param {string} OtpLoginRequest.email - The email address of the user
 * @param {string} OtpLoginRequest.otp - The OTP code entered by the user
 * @returns
 */
const otpLogin = async (OtpLoginRequest) => {
  try {
    const response = await axios.post(ENDPOINTS.OTP_LOGIN_URL, OtpLoginRequest,
      { withCredentials: true }
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error OTP login:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to login",
    };
  }
};
/**
 *
 * @param {object} passwordLoginRequest
 * @param {string} passwordLoginRequest.email - The email address of the user
 * @param {string} passwordLoginRequest.password - The password of the user
 * @returns
 */
const passwordLogin = async (passwordLoginRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.PASSWORD_LOGIN_URL,
      passwordLoginRequest,
      { withCredentials: true }
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error in password login:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to login",
    };
  }
};


const sendForgotPasswordOTP = async (email) => {
  try {
    const response = await axios.post(
      `${ENDPOINTS.FORGOT_PASSWORD_SENDOTP_URL}?email=${email}`
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error sending forgot password OTP:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to send OTP",
    };
  }
}

/**
 * 
 * @param {object} otpRequest 
 * @param {string} otpRequest.email - The email address of the user
 * @param {string} otpRequest.otp - The OTP code entered by the user
 * @returns 
 */
const verifyForgotPasswordOTP = async (otpRequest) => {
  try {
    const response = await axios.post(
      ENDPOINTS.FORGOT_PASSWORD_VERIFY_OTP_URL,
      otpRequest
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error verifying forgot password OTP:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to verify OTP",
    };
  }
};

/**
 * 
 * @param {object} resetPasswordRequest 
 * @param {string} resetPasswordRequest.email - The email address of the user
 * @param {string} resetPasswordRequest.password - The new password for the user
 * @param {string} token - The token received after verifying the OTP
 * @returns 
 */
const resetPassword = async (resetPasswordRequest,token) => {
  try {
    const response = await axios.post(
      ENDPOINTS.FORGOT_PASSWORD_SUBMIT_URL,
      resetPasswordRequest,
      {
        headers: {
          Authorization: token,
        },
      }
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error resetting password:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to reset password",
    };
  }
};

//just check the auth token is valid or not
const checkAuthToken = async () => {
  try {

    const response = await axios.get(ENDPOINTS.CHECK_AUTH_TOKEN_URL, {
      withCredentials: true ,
    });
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error checking auth token:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to check auth token",
    };
  }
};

const extractTokenFromCookie = (cookieString) => {
  if (!cookieString) return null;
  
  const match = cookieString.match(/Authorization=([^;]+)/);
  return match ? match[1] : null;
};

const logout = async () => {
  try {
    const response = await axios.post(
      ENDPOINTS.LOGOUT_URL,
      {}, // <-- empty request body
      {
        withCredentials: true, // ✅ correctly placed here
      }
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error logging out:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to logout",
    };
  }
}

const getProfilePic = async (email) => {
  try {
    const response = await axios.get(`${ENDPOINTS.PROFILE_PIC_URL}?email=${email}`, {
      withCredentials: true,
    });
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    console.error("Error fetching profile picture:", error);
    if (error.response?.data) {
      // Return the error data from the server
      return {
        success: false,
        error: error.response.data,
      };
    }

    return {
      success: false,
      error: error.message || "Failed to fetch profile picture",
    };
  }
}

export {
  sendSignUpOTP,
  verifySignUpOTP,
  registerOAuthUser,
  preFormRegisterCheck,
  registerFormUser,
  checkEmail,
  isEmailExists,
  checkPhone,
  checkUserOtpVrfd,
  sendLoginOTP,
  otpLogin,
  passwordLogin,
  sendForgotPasswordOTP,
  verifyForgotPasswordOTP,
  resetPassword,
  checkAuthToken,
  extractTokenFromCookie,
  getProfilePic,
  logout,
};
