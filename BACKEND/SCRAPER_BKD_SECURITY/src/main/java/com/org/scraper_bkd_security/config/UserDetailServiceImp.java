package com.org.scraper_bkd_security.config;

import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.repo.UserRepo;
import com.org.scraper_bkd_security.util.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailServiceImp implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtils.class);
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Customer customer = userRepo.findUserByEmail(username);
            if (customer == null) {
                throw new UsernameNotFoundException("Username not found with : " + username);
            }
            // Check if role is null or empty and provide a default value
            String role = customer.getRole();
            if (role == null || role.trim().isEmpty()) {
                role = "ROLE_USER"; // Default role
            }

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            return new User(customer.getEmail(), customer.getPassword(), authorities);
        }
        catch(Exception e){
            logger.error("Error at loadUserByUsername : {}",e.getMessage());
            throw new RuntimeException("Unable to login, try again");
        }
    }
}
