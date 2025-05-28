package com.org.scraper_bkd.constants;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;

public final class AppConstant {

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
    public static final String JWT_ISSUER="buzzlyn";
    public static final String PINCODE_TRACKER_ENDPOINT="/fetch_pincodeTracking";
    public static final String PRICE_SCRAPER_ENDPOINT="/scrape_price";
    public static final List<String> URL_IDENTIFIER_SUPPORTED_BRANDS =
            List.of("amazon.in", "flipkart.com");
    public static final List<String> PINCODE_SUPPORTED_BRANDS =
            List.of("amazon.in", "flipkart.com");

    public static final Map<String, String> CURRENCY_SYMBOLS = new HashMap<>();
    static {
        CURRENCY_SYMBOLS.put("USD", "$");
        CURRENCY_SYMBOLS.put("EUR", "€");
        CURRENCY_SYMBOLS.put("GBP", "£");
        CURRENCY_SYMBOLS.put("INR", "₹");
        CURRENCY_SYMBOLS.put("JPY", "¥");
        CURRENCY_SYMBOLS.put("CNY", "¥");
        CURRENCY_SYMBOLS.put("AUD", "A$");
        CURRENCY_SYMBOLS.put("CAD", "C$");
        CURRENCY_SYMBOLS.put("CHF", "CHF");
        CURRENCY_SYMBOLS.put("KRW", "₩");
        CURRENCY_SYMBOLS.put("RUB", "₽");
        CURRENCY_SYMBOLS.put("BRL", "R$");
        CURRENCY_SYMBOLS.put("ZAR", "R");
        // Add more if needed
    }

    public static final String WEBPUSH_PUBLIC_KEY=getEnv("WEBPUSH_PUBLIC_KEY");
    public static final String WEBPUSH_PRIVATE_KEY=getEnv("WEBPUSH_PRIVATE_KEY");
    public static final String WEBPUSH_MAIL=getEnv("WEBPUSH_MAIL");
    public static final String AMAZON_IND="amazon.in";
    public static final String FLIPKART="flipkart.com";
    public static final String BREVO_USERNAME=getEnv("BREVO_USERNAME");
    public static final String BREVO_PASSWORD=getEnv("BREVO_PASSWORD");
    public static final String BUZZLYN_LOGO="https://i.ibb.co/pvxgbFLW/g18.png";
    public static final String DASHBOARD_URL="https://buzzlyn.com/dashboard";
    public static final String CLOUDINARY_URL=getEnv("CLOUDINARY_URL");
    public static final String PYTHON_SCRAPER_URL=getEnv("PYTHON_SCRAPER_URL");
    public static final String PYTHON_SCRAPER_SECRETKEY=getEnv("PYTHON_SCRAPER_SECRETKEY");
    public static final String FRONTEND_BASE_URL=getEnv("FRONTEND_BASE_URL");
    public static final String GEMINI_API_KEY=getEnv("GEMINI_API_KEY");

}

