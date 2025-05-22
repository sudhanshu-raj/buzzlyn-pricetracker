package com.org.scraper_bkd.controller;

import com.org.scraper_bkd.model.PriceTrackerUsers;
import com.org.scraper_bkd.repo.PriceTrackerUserRepo;
import com.org.scraper_bkd.service.PriceTrackerUserService;
import com.org.scraper_bkd.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/tst")
@RequiredArgsConstructor
public class testingController {

    private static final Logger logger = LoggerFactory.getLogger(testingController.class);


        @GetMapping("/health")
        public ResponseEntity<String> healthCheck(){
            return ResponseEntity.ok("Server is UP");
        }

}
