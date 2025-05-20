package com.org.scraper_bkd_security.constants;

import io.github.cdimascio.dotenv.Dotenv;

public final class ApplicationConstants {

    // Use a try-catch to gracefully handle missing .env file
    static Dotenv dotenv;
    static {
        try {
            dotenv = Dotenv.load();
        } catch (Exception e) {
            // Fall back to an empty implementation if .env file is not found
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }
    }

    // Use System.getenv() as fallback when dotenv returns null
    private static String getEnv(String key) {
        String value = dotenv.get(key);
        return value != null ? value : System.getenv(key);
    }

    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String JWT_SECRET_DEFAULT_VALUE = getEnv("JWT_SECRET");
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_ISSUER="PriceRadar";
    public static final long OTPID_KEY=100000;
    public static final String FRONTEND_BASE_URL=getEnv("FRONTEND_BASE_URL");
    public static final String BACKEND_SPRING_MAIN_MODULE=getEnv("BACKEND_SPRING_MAIN_MODULE");
    public static final long JWT_EXPIRATION= 30L * 24 * 60 * 60 * 1000;   // 30 days
    public static final long OTP_EXPIRE_TIME =5;
    public static final String LOGINOTP_PREFIX="loginOtp:";
    public static final String FORGETPASSWORD_OTP_PREFIX_="forgetPass:";
    public static final long FORGETPASSWORD_TOKEN_EXPIRATION= 5*60*1000;
    public static final int PROFILE_IMG_SIZE=88;
    public static final String LOCALHOST=getEnv("LOCALHOST");
    public static final String REDIS_HOST=getEnv("localhost");

}
