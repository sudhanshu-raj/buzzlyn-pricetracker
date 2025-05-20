package com.org.scraper_bkd_security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.FRONTEND_BASE_URL;

@Component
public class TestOAuth2Handler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        System.out.println("inside test handler:\n");
        System.out.println(oauthToken);

        OAuth2User oauthUser = oauthToken.getPrincipal();

        System.out.println("name::"+oauthUser.getName());
        Map<String, Object> user_attributes= oauthUser.getAttributes();
        System.out.println(user_attributes);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, String> jsonResponse = new HashMap<>();
        jsonResponse.put("name",(String)user_attributes.get("given_name"));
        jsonResponse.put("redirectUrl", FRONTEND_BASE_URL + "/verify-phone");
        jsonResponse.put("message", "Please verify your phone.");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(jsonResponse);

        response.getWriter().write(json);

    }
}
