package com.org.scraper_bkd_security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.scraper_bkd_security.config.EmailOtpAuthenticationToken;
import com.org.scraper_bkd_security.dto.OtpLoginRequest;
import com.org.scraper_bkd_security.util.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.JWT_HEADER;

public class OtpAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtTokenUtils jwtUtil;

    public OtpAuthenticationFilter(AuthenticationManager authManager, JwtTokenUtils jwtUtil) {
        super(new AntPathRequestMatcher("/auth/login-otp", "POST"));
        setAuthenticationManager(authManager);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        OtpLoginRequest loginRequest = mapper.readValue(request.getInputStream(), OtpLoginRequest.class);

        return getAuthenticationManager().authenticate(
                new EmailOtpAuthenticationToken(loginRequest.getEmail(), loginRequest.getOtp())
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {

        String email = authResult.getName();
        String jwt = jwtUtil.generateToken(email,"ROLE_USER","");

        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(JWT_HEADER, jwt)));
        response.getWriter().flush();
    }
}
