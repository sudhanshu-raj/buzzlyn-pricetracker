package com.org.scraper_bkd_security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.repo.UserRepo;
import com.org.scraper_bkd_security.services.SignUpService;
import com.org.scraper_bkd_security.util.ImageUtils;
import com.org.scraper_bkd_security.util.JwtTokenUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.*;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final UserRepo userRepo;
    private final JwtTokenUtils jwtTokenUtils;
    private final @Lazy PasswordEncoder passwordEncoder;
    private final ImageUtils  imageUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauthUser = oauthToken.getPrincipal();
            
            String email = oauthUser.getAttribute("email");
            String firstName = oauthUser.getAttribute("given_name");
            System.out.println("user :" + email + " authenticated using oauth");
            Customer newUser = null;
            // Check if user exists in DB
            Optional<Customer> userOpt = userRepo.findByEmail(email);

            // Generate JWT token
            String jwt = null;
            if (userOpt.isPresent() && userOpt.get().isPhoneVerified()) {
                System.out.println("seems user phone is verified");
                jwt = jwtTokenUtils.generateToken(email, "ROLE_USER",userOpt.get().getPhoneNumber());
            }
            //if data not there means new user
            if (!userOpt.isPresent()) {
                Map<String, Object> user_attributes = oauthUser.getAttributes();
                String imageURL= (String) user_attributes.get("picture");
                byte[] imageBytes=imageUtils.downloadImage(imageURL);

                newUser = new Customer();
                newUser.setEmail(email);
                newUser.setFirstName((String) user_attributes.get("given_name")); // optional
                newUser.setEmailVerified(true);
                newUser.setPhoneVerified(false);
                newUser.setGoogleOAuthUser(true);
                String randomPassword = UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(randomPassword));
                newUser.setPhoneNumber("PENDING");
                newUser.setRole("USER");

                if(imageBytes!=null){
                    newUser.setImageData(imageBytes);
                }
                userRepo.save(newUser);
            }
            else{
                newUser=userOpt.get();
            }

            String profilePicBase64 = null;
            if (newUser.getImageData() != null) {
                profilePicBase64 = Base64.getEncoder().encodeToString(newUser.getImageData());
            }
            String number=null;
            if(newUser.getPhoneNumber()!=null){
                number=newUser.getPhoneNumber();
            }


            // Build redirect URL with parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(FRONTEND_BASE_URL + "/auth/callback")
                    .queryParam("email", email)
                    .queryParam("number",number)
                    .queryParam("firstName", (firstName != null ? firstName : ""))
                    .queryParam("newUser", (userOpt.isPresent() ? "false" : "true"))
                    .queryParam("isVerified", (userOpt.isPresent() && userOpt.get().isPhoneVerified() ? "true" : "false"));

            String redirectUrl = builder.toUriString();

            //request.getSession().setAttribute("profilePicBase64", profilePicBase64);
           // response.addCookie(createProfilePicCookie("profilePicBase64", profilePicBase64));
            if (jwt != null) {
                response.addCookie(createSecureCookie2(JWT_HEADER, jwt));
               // createSecureCookie(JWT_HEADER, jwt,response);
            }
            // Redirect to frontend
            response.sendRedirect(redirectUrl);
        }catch (Exception e) {
            logger.error("Error in oauth2successhandler : {}",e.getMessage());
           // response.sendRedirect(FRONTEND_BASE_URL + "/auth/error");
        }

    }


    public Cookie createSecureCookie2(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); 
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax"); // Prevents CSRF attacks
        cookie.setMaxAge((int)(JWT_EXPIRATION / 1000));
        cookie.setDomain(LOCALHOST);// Set expiration to match JWT
        return cookie;
    }



}
