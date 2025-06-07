package com.org.scraper_bkd_security.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.BREVO_API_KEY;

public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void  sendEmail(String recipient,String subject, String template){
        try{
            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", BREVO_API_KEY);

            // JSON payload
            Map<String, Object> body = new HashMap<>();
            Map<String, String> sender = Map.of("name", "buzzlyn", "email", "noreply@buzzlyn.com");
            Map<String, String> to = Map.of("email", recipient, "name", "User");

            body.put("sender", sender);
            body.put("to", List.of(to));
            body.put("subject", subject);
            body.put("htmlContent", template);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Email sent: {}", response.getBody());

        }
        catch (Exception e){
            logger.error("Error while sending email at sendEmail : {}",e.getMessage());
            throw new RuntimeException("Error while sending email at sendEmail : "+e.getMessage());
        }
    }

    public static String loadTemplate(String fullTemplateName) throws IOException {
        Resource resource = new ClassPathResource("templates/email/" + fullTemplateName);
        if (!resource.exists()) {
            throw new FileNotFoundException("Template file not found: " + fullTemplateName);
        }
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
