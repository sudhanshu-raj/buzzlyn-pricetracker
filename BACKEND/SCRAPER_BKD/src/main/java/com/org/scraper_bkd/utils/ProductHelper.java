package com.org.scraper_bkd.utils;

import com.org.scraper_bkd.service.ProductScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.org.scraper_bkd.constants.AppConstant.*;

@Component
public class ProductHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProductHelper.class);

    public  String extractBrand(String urlString) {
        try {
            URL url = new URL(urlString);
            String host = url.getHost();

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            return host;
        } catch (Exception e) {
            logger.error("Error while extract domain name from url : {}", e.getMessage());
            return null;
        }
    }

    public boolean isPincodeTrackingSupports(String url){
        try{
            String brand=extractBrand(url);
           return PINCODE_SUPPORTED_BRANDS.contains(brand);
        }
        catch (Exception e) {
            logger.error("Error at isPincodeTrackingSupports: {}", e.getMessage());
            return false;
        }
    }

    public  String extractProductNameFromUrl(String url) {
        try {
            String withoutProtocol = url.replaceFirst("https?://", "");

            int firstSlashIndex = withoutProtocol.indexOf('/');
            if (firstSlashIndex == -1) return null;

            String path = withoutProtocol.substring(firstSlashIndex + 1);
            String[] segments = path.split("/");

            // First segment is typically the product slug
            if (segments.length > 0) {
                return segments[0];
            }
        } catch (Exception e) {
           logger.error("Error at extractProductNameFromUrl : {}",e.getMessage());
        }
        return null;
    }

    public  String getFlipkartProductIdentifier(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();

            // Check for pid in query parameters
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("pid")) {
                        return pair[1];
                    }
                }
            }

            // Fallback: extract from /p/<product_slug>
            Pattern pattern = Pattern.compile("/p/([^/?]+)");
            Matcher matcher = pattern.matcher(uri.getPath());
            if (matcher.find()) {
                return matcher.group(1);
            }
            return url;

        } catch (URISyntaxException e) {
           logger.warn("Invalid url at getFlipkartProductIdentifier : {}",e.getMessage());
        }

        return null;
    }
    public String getAmazonProductIdentifier(String url){
        try {
            URI uri = new URI(url);
            String path = uri.getPath();

            // Regex to match ASIN in formats like /dp/B0CHX1K9VK or /gp/product/B0CHX1K9VK
            Pattern pattern = Pattern.compile("/(?:dp|gp/product)/([A-Z0-9]{10})");
            Matcher matcher = pattern.matcher(path);

            if (matcher.find()) {
                System.out.println(matcher);
                return matcher.group(1); // The ASIN
            }
            return url;

        } catch (URISyntaxException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        }

        return null;
    }

    public static String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) {
            return "";
        }
        String code = currencyCode.toUpperCase();
        return CURRENCY_SYMBOLS.getOrDefault(code, code);
    }

    public static String formatPrice(long price,String currencyCode){
        try{
            String priceStr=String.valueOf(price);
            int length=priceStr.length();
            if(currencyCode.equalsIgnoreCase("INR")){

                if(length<4) return priceStr;

                String lastThree=priceStr.substring(length-3,length);
                String beforeThree=priceStr.substring(0,length-3);
                StringBuilder ss= new StringBuilder();
                int count=0;
                for(int i=beforeThree.length()-1;i>=0;i--){
                    ss.append(beforeThree.charAt(i));
                    count++;
                    if(count==2 && i!=0){
                        ss.append(',');
                        count=0;
                    }
                }

                ss.reverse().append(',').append(lastThree);
                return ss.toString();

            }
            else{
                if (length <= 3) return priceStr;

                StringBuilder sb = new StringBuilder();
                int count = 0;

                for (int i = length - 1; i >= 0; i--) {
                    sb.append(priceStr.charAt(i));
                    count++;
                    if (count == 3 && i != 0) {
                        sb.append(',');
                        count = 0;
                    }
                }
                return sb.reverse().toString();
            }
        }
        catch(Exception e){
            logger.error("Error while formating price : {}",e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        String url="https://www.robu.com/Symbol-Premium-Linen-Solid-Casual/dp/B0CR6LDGBG/ref=sr_1_2_sspa?sr=8-2-spons&sp_csd=d2lkZ2V0TmFtZT1zcF9hdGY&psc=1";
        //url="https://www.amazon.in/Majestic-Man-Classic-Cotton-Casual/dp/B0CK6LC8QR/ref=sr_1_7?sr=8-7";
        //url="https://www.jiomart.com/p/fashion/tazo-mens-round-neck-printed-t-shirt-oversized-t-shirts-loose-fit-elbow-length-sleeve-t-shirt-t-shirt-for-mens-mens-t-shirt-mens-tshirt-tshirt-for-mens-t-shirts-tshirts-funky-t-shirt-mens-tshirt/609351529";

        //url="https://www.flipkart.com/atheros-ar9271l-lite-wifi-adapter-kali-linux-packet-injection-monitor-mode-usb/p/itm495b303358e98";
       // System.out.println(getAmazonProductIdentifier(url));
        ProductHelper obj=new ProductHelper();
        System.out.println(formatPrice(123456,"USD"));

    }
}
