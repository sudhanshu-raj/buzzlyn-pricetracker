package com.org.scraper_bkd.service.notifications;

import com.org.scraper_bkd.enums.NotificationFrequency;
import com.org.scraper_bkd.model.PriceTrackerModel;
import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.model.ProductScraperModel;
import com.org.scraper_bkd.repo.PriceTrackerRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.org.scraper_bkd.constants.AppConstant.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final PriceTrackerRepo priceTrackerRepo;

    public static void sendEmail(String recipient,String subject, String template) throws MessagingException {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp-relay.brevo.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(BREVO_USERNAME, BREVO_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@buzzlyn.com","buzzlyn"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(template,"text/html; charset=utf-8");

            Transport.send(message);
        }
        catch(Exception e){
            logger.error("Error while sending email : {}",e.getMessage());
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





    public static void main(String[] args) throws Exception {
        String htmlTemplate = loadTemplate("src/main/resources/templates/email/productUpdates.template");

// Replace placeholders
//        String finalHtml = htmlTemplate
//                .replace("{logoUrl}","https://i.ibb.co/pvxgbFLW/g18.png")
//                .replace("{brand}", "amazon")
//                .replace("{productName}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G Graphics Card, WINDFORCE Cooling System, 32GB 512-Bit GDDR7, GV-N5090WF3OC-32GD Video Card")
//                .replace("{oldPrice}", "₹425,569")
//                .replace("{newPrice}", "5,400")
//                .replace("{savings}","₹120,169")
//                .replace("{currencySymbol}","₹")
//                .replace("{pincode}","847304")
//                .replace("{savingsPercentage}","29")
//                .replace("{productImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
//                .replace("{productUrl}", "https://amzn.to/3GHRMun");

        String finalHtml = htmlTemplate
                // Values from the Java example
                .replace("{logoUrl}", "https://i.ibb.co/pvxgbFLW/g18.png")
                .replace("{brand}", "amazon")
                .replace("{productName}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G ...")
                .replace("{oldPrice}", "₹425,569")
                .replace("{newPrice}", "₹5,400")
                .replace("{savings}", "₹120,169")
                .replace("{currencySymbol}", "₹")
                .replace("{pincode}", "847304")
                .replace("{savingsPercentage}", "29")
                .replace("{productImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
                .replace("{productUrl}", "https://amzn.to/3GHRMun")

                // Additional variables from JavaScript with their values
                .replace("{newPriceOnly}", "5400")
                .replace("{stockStatus}", "In Stock")
                .replace("{mainProductUrl}", "https://amzn.to/3GHRMun") // Using productUrl from Java example
                .replace("{dashboardUrl}", "https://buzzlyn.com/dashboard")
                .replace("{graph}", "https://i.ibb.co/WWgZn7HM/user-Graph.png")

                // Product 1 details
                .replace("{product1ImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
                .replace("{product1Brand}", "amazon")
                .replace("{product1Name}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G...")
                .replace("{product1NewPrice}", "₹80")
                .replace("{product1OldPrice}", "₹100")
                .replace("{product1StockStatus}", "In Stock")
                .replace("{product1Url}", "https://amzn.to/product1")

                // Product 2 details
                .replace("{product2ImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
                .replace("{product2Brand}", "amazon")
                .replace("{product2Name}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G...")
                .replace("{product2NewPrice}", "₹80")
                .replace("{product2OldPrice}", "₹100")
                .replace("{product2StockStatus}", "In Stock")
                .replace("{product2Url}", "https://amzn.to/product2")

                // Product 3 details
                .replace("{product3ImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
                .replace("{product3Brand}", "amazon")
                .replace("{product3Name}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G...")
                .replace("{product3NewPrice}", "₹80")
                .replace("{product3OldPrice}", "₹100")
                .replace("{product3StockStatus}", "In Stock")
                .replace("{product3Url}", "https://amzn.to/product3");

        //sendEmail(finalHtml);
    }
}
