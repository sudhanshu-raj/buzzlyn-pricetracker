package com.org.scraper_bkd_security.config;

import com.org.scraper_bkd_security.exceptions.CustomAuthenticationEntryPoint;
import com.org.scraper_bkd_security.filters.JWTTokenGeneratorFilter;
import com.org.scraper_bkd_security.filters.JWTTokenValidatorFilter;
import com.org.scraper_bkd_security.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.BACKEND_SPRING_MAIN_MODULE;
import static com.org.scraper_bkd_security.constants.ApplicationConstants.FRONTEND_BASE_URL;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    private final OAuth2AuthorizationRequestResolver customResolver;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final @Lazy OAuth2SuccessHandler oAuth2SuccessHandler;
    private final EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,AuthenticationManager authManager)
        throws Exception{


        http

//                .headers(headers -> headers
//                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' https://trusted-cdn.com; style-src 'self' 'unsafe-inline'"))
//                        .frameOptions(frameOptions -> frameOptions.deny())
//                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((requests) -> requests
                    .requestMatchers(
                            "/error","/redis/*","/auth/**", "/oauth2/**", "/login/**","/tst/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())  // Disable default login form
                .httpBasic(basic -> basic.disable())  // Disable HTTP Basic if you don't need it
                .httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .oauth2Login(oath -> oath
//                        .authorizationEndpoint(config -> config
//                                .authorizationRequestResolver(customResolver))
//                        .authorizedClientService(authorizedClientService)
                        .successHandler(oAuth2SuccessHandler)
//
                )
                .authenticationManager(authManager)
                ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                FRONTEND_BASE_URL,
                BACKEND_SPRING_MAIN_MODULE  // 👈 Add this
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) throws Exception {

        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        // Register Email OTP provider
        authBuilder.authenticationProvider(emailOtpAuthenticationProvider);

        // Register DAO Provider for username/password
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userDetailsService);
        daoProvider.setPasswordEncoder(passwordEncoder);
        authBuilder.authenticationProvider(daoProvider);

        // Make sure OAuth2 Login provider is registered
        OAuth2LoginAuthenticationProvider oauth2LoginProvider =
                new OAuth2LoginAuthenticationProvider(
                        new DefaultAuthorizationCodeTokenResponseClient(),
                        new DefaultOAuth2UserService());
        authBuilder.authenticationProvider(oauth2LoginProvider);

        // For OpenID Connect (like Google)
        OidcAuthorizationCodeAuthenticationProvider oidcProvider =
                new OidcAuthorizationCodeAuthenticationProvider(
                        new DefaultAuthorizationCodeTokenResponseClient(),
                        new OidcUserService());
        authBuilder.authenticationProvider(oidcProvider);

        return authBuilder.build();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
}
