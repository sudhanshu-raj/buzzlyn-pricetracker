package com.org.scraper_bkd.service;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;


public class Testing {



    public static void main(String[] args) {
            String token="Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJQcmljZVJhZGFyIiwic3ViIjoicmFqc3VkaGFuc2h1OTQzMUBnbWFpbC5jb20iLCJhdXRob3JpdGllcyI6IlJPTEVfVVNFUiIsImlhdCI6MTc0NTM1NzM5NywiZXhwIjoxNzQ3OTQ5Mzk3fQ.7bVEsJECZA1vO0FOrUQFz0D1IzJD8ugXbYikGTEAzJE";
            token=token.substring(7);
        System.out.println(token);
    }
}
