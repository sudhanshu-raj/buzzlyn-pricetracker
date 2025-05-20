package com.org.scraper_bkd.service.notifications;

import static com.org.scraper_bkd.service.notifications.EmailService.loadTemplate;
import static com.org.scraper_bkd.service.notifications.EmailService.sendEmail;

public class ProductUpdatesNotification {

    static class ProductCard{
        private final String productImageUrl;
        private final String productBrand;
        private final String productName;
        private final String productPriceStatus;
        private final String productNewPrice;
        private final String productStockStatus;
        private final String productUrl;


        ProductCard(String productImageUrl, String productBrand, String productName, String productPriceStatus, String productNewPrice, String productStockStatus, String productUrl) {
            this.productImageUrl = productImageUrl;
            this.productBrand = productBrand;
            this.productName = productName;
            this.productPriceStatus = productPriceStatus;
            this.productNewPrice = productNewPrice;
            this.productStockStatus = productStockStatus;
            this.productUrl = productUrl;
        }

        public String getProductPriceStatus() {
            return productPriceStatus;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public String getProductBrand() {
            return productBrand;
        }

        public String getProductName() {
            return productName;
        }

        public String getProductNewPrice() {
            return productNewPrice;
        }

        public String getProductStockStatus() {
            return productStockStatus;
        }

        public String getProductUrl() {
            return productUrl;
        }

    }


    public static void main(String[] args) throws Exception {
        String htmlTemplate = loadTemplate("src/main/resources/templates/email/productUpdates.template");
        String productCardTemplate= loadTemplate("src/main/resources/templates/email/productUpdates_ProductCard.template");

        String imageUrl="https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg";
        String brand="amazon";
        String name="GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G ...";
        String priceStatus="\uD83D\uDD3B Price dropped from ₹100 to ₹80";
        String newPrice="₹100";
        String stock="In Stock";
        String url="https://amzn.to/product1";
        ProductCard productCard= new ProductCard(imageUrl,brand,name,priceStatus,newPrice,stock,url);

        String modifiedProductCard=productCardTemplate
                .replace("{productImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
                .replace("{productBrand}", "amazon")
                .replace("{productName}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G...")
                .replace("{productNewPrice}", "₹80")
                .replace("{productPriceStatus}","🔻 Price dropped from ₹100 to ₹80")
                .replace("{productStockStatus}", "In Stock")
                .replace("{productUrl}", "https://amzn.to/product1");


        String finalHtmlTemplate=htmlTemplate
                // Values from the Java example
                .replace("{logoUrl}", "https://i.ibb.co/pvxgbFLW/g18.png")
                .replace("{brand}", "amazon")
                .replace("{productName}", "GIGABYTE Geforce RTX 5090 WINDFORCE OC pci_e_x16 32G ...")
                .replace("{oldPrice}", "₹425,569")
                .replace("{newPrice}", "₹5,400")
                .replace("{savings}", "₹120,169")
                .replace("{currencySymbol}", "₹")
                .replace("{pincode}", "847304")
                .replace("{productImageUrl}", "https://m.media-amazon.com/images/I/71hOB-2Rf5L._SL1500_.jpg")
                .replace("{productUrl}", "https://amzn.to/3GHRMun")
                .replace("{newPriceOnly}", "5400")
                .replace("{stockStatus}", "In Stock")
                .replace("{mainProductUrl}", "https://amzn.to/3GHRMun") // Using productUrl from Java example
                .replace("{dashboardUrl}", "https://buzzlyn.com/dashboard")
                .replace("{graph}", "https://i.ibb.co/WWgZn7HM/user-Graph.png")
                .replace("{timePeriod}","Weekly")
                .replace("{priceStatus}","🔻 Price dropped from ₹100 to ₹80")

                // Replace Product Cards
                .replace("<!--otherProduct-->","Other Products")
                .replace("<!--product1-->",modifiedProductCard)
                .replace("<!--product2-->",modifiedProductCard);
//                .replace("<!--product3-->",modifiedProductCard);

        //sendEmail(finalHtmlTemplate);


    }

}
