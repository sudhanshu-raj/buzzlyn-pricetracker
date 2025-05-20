package com.org.scraper_bkd_security.util;

import org.owasp.encoder.Encode;


public class SanitizationUtil {

    public static String sanitize(String input) {
        return input == null ? null : Encode.forHtml(input);
    }

    public static void main(String[] args) {
        String before= """
                Firs'tname
                """;

        System.out.println(sanitize(before));
    }
}
