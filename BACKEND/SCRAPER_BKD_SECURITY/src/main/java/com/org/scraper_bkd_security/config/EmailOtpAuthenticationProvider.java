package com.org.scraper_bkd_security.config;

import com.org.scraper_bkd_security.services.SignInOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.LOGINOTP_PREFIX;

@Component
@RequiredArgsConstructor
public class EmailOtpAuthenticationProvider implements AuthenticationProvider {


    private final SignInOtpService signInOtpService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getPrincipal().toString();
        String otp = authentication.getCredentials().toString();

        if (signInOtpService.verifyOtp(email, otp,LOGINOTP_PREFIX)) {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            return new EmailOtpAuthenticationToken(email, authorities);
        }

        throw new BadCredentialsException("Invalid OTP");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailOtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
