export const SECURITY_BASE_URL = import.meta.env.VITE_SECURITY_BASE_URL || "http://localhost:8081";
export const AUTH_BASE_URL = `${SECURITY_BASE_URL}/auth`;
export const SCRAPER_BASE_URL=import.meta.env.VITE_SCRAPER_BASE_URL || "http://localhost:8082";

export const ENDPOINTS = {
    AUTH_BASE_URL,
    GOOGLE_OAUTH_URL: `${SECURITY_BASE_URL}/oauth2/authorization/google`,
    SIGNUP_OTP_SEND_URL: `${AUTH_BASE_URL}/signUpOtpSend`,
    SIGNUP_OTP_VERIFY_URL: `${AUTH_BASE_URL}/signUpOtpVerify`,
    PREREGISTER_CHECK_URL: `${AUTH_BASE_URL}/preRegisterCheck`,
    REGISTER_FORMUSER_URL: `${AUTH_BASE_URL}/register`,
    REGISTER_OAUTH_URL: `${AUTH_BASE_URL}/registerOthUsr`,
    CHECK_EMAIL_URL: `${AUTH_BASE_URL}/checkEmail`,
    CHECK_PHONE_URL: `${AUTH_BASE_URL}/checkPhone`,
    OTP_VERIFICATION_NEEDED_URL: `${AUTH_BASE_URL}/checkUsrOtpVrfd`,
    ISEMAIL_EXISTS_URL: `${AUTH_BASE_URL}/isEmailExists`,
    LOGINOTP_SEND_URL: `${AUTH_BASE_URL}/loginRequest-otp`,
    OTP_LOGIN_URL: `${AUTH_BASE_URL}/otpLogin`,
    PASSWORD_LOGIN_URL: `${AUTH_BASE_URL}/passwordLogin`,
    FORGOT_PASSWORD_SENDOTP_URL: `${AUTH_BASE_URL}/forgetPass-otp`,
    FORGOT_PASSWORD_VERIFY_OTP_URL: `${AUTH_BASE_URL}/verifyForgetPass-otp`,
    FORGOT_PASSWORD_SUBMIT_URL: `${AUTH_BASE_URL}/reset-password`,
    CHECK_AUTH_TOKEN_URL: `${AUTH_BASE_URL}/authTokenCheck`,
    CHECK_AUTH_TOKEN_HEADR_URL: `${AUTH_BASE_URL}/authTokenCheckHeader`,
    LOGOUT_URL: `${AUTH_BASE_URL}/logout`,
    PROFILE_PIC_URL: `${AUTH_BASE_URL}/profilePic`,
    PRODUCT_SCRAPE_URL: `${SCRAPER_BASE_URL}/fetchProduct`,
    PRODUCT_TRACK_URL: `${SCRAPER_BASE_URL}/priceTrackerRequest`,
    FETCH_USER_TRACKED_PRODUCTS_URL: `${SCRAPER_BASE_URL}/fetchUserProducts`,
    FETCH_USER_CONFIG_URL: `${SCRAPER_BASE_URL}/fetchUserConfig`,
    UPDATE_USER_CONFIG_URL: `${SCRAPER_BASE_URL}/updateUserConfig`,
    FETCH_PRICE_HISTORY_URL: `${SCRAPER_BASE_URL}/fetchPriceHistory`,
    DELETE_TRACKED_PRODUCT_URL: `${SCRAPER_BASE_URL}/deleteTracker`,
    FETCH_TRACKER_BY_PRODUCT_ID_URL: `${SCRAPER_BASE_URL}/fetchExistedProductTracker`,
    PUSH_NOTIFICATION_SUBSCRIBE_URL: `${SCRAPER_BASE_URL}/subscribe`,
    PUSH_NOTIFICATION_UNSUBSCRIBE_URL: `${SCRAPER_BASE_URL}/unsubscribe`,

  };