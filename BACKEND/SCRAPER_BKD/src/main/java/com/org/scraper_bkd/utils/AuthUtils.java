package com.org.scraper_bkd.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class AuthUtils {

    private AuthUtils() {}

    public static Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static String email() {
        return auth().getName();
    }

    public static String phoneNumber() {
        Authentication a = auth();
        if (a instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getClaim("phoneNumber");
        }
        return null;   // or throw if you want to enforce JWT only
    }
}
