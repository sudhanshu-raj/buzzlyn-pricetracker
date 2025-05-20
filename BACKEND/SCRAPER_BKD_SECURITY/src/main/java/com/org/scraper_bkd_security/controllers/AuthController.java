package com.org.scraper_bkd_security.controllers;

import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepo userRepo;

    @GetMapping("/current-session")
    public ResponseEntity<Map<String, String>> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getAttribute("email");
        Map<String, String> response = new HashMap<>();
        response.put("email", email);

        Optional<Customer> userOpt = userRepo.findByEmail(email);
        response.put("newUser", userOpt.isPresent() ? "false" : "true");
        response.put("isVerified", userOpt.isPresent() && userOpt.get().isPhoneVerified() ? "true" : "false");

        return ResponseEntity.ok(response);
    }
}
