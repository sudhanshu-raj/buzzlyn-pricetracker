package com.org.scraper_bkd_security.config;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class EmailOtpAuthenticationToken  extends AbstractAuthenticationToken {

    private final Object principal;
    private final String otp;

    public EmailOtpAuthenticationToken(String email, String otp) {
        super(null);
        this.principal = email;
        this.otp = otp;
        setAuthenticated(false);
    }

    public EmailOtpAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.otp = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return otp;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
