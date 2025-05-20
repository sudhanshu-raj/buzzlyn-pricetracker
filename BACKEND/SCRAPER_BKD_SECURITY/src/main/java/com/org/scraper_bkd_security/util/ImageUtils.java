package com.org.scraper_bkd_security.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

@Component
public class ImageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    public  byte[] downloadImage(String imgURL) throws IOException {
        try {
            URL url = new URL(imgURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Connection", "keep-alive");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return outputStream.toByteArray();
        }
        catch(Exception e){
            logger.error("Error while downloading image : {}",e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        String url="https://lh3.googleusercontent.com/a/ACg8ocK2SCFIBorQXfj1gJZZQkIWRMm7gt1rt1Cacu45kKRAEP1wzO5yCA=s96-c";
        ImageUtils imageUtils=new ImageUtils();
        byte[] imagebytes=imageUtils.downloadImage(url);
        if(imagebytes!=null){
            System.out.println(Arrays.toString(imagebytes));
            System.out.println("image downladed");
        }
        else{
            System.out.println("failed to download the image");
        }
    }

}
