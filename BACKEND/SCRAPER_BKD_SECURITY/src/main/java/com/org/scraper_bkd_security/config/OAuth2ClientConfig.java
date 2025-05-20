package com.org.scraper_bkd_security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

/*
This class I using for custom oauth request where I'm asking for refresh token also from the Google,
Like generally google not returns refresh token after authentication so we have make custom request for that.
Although I'm not using refresh token currently in this app now so this class is not much important
But in future if we want to verify refresh token then this is the setup done already.
 */

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
            ClientRegistrationRepository repo) {

        // base URI for OAuth2 login
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

        // wrap and customize the resolver
        return new OAuth2AuthorizationRequestResolver() {

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest original = defaultResolver.resolve(request);
                return customize(original);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest original = defaultResolver.resolve(request, clientRegistrationId);
                return customize(original);
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest original) {
                if (original == null) return null;

                Map<String, Object> additionalParameters = new HashMap<>(original.getAdditionalParameters());
                additionalParameters.put("access_type", "offline");
                additionalParameters.put("prompt", "consent");

                return OAuth2AuthorizationRequest.from(original)
                        .additionalParameters(additionalParameters)
                        .build();
            }
        };
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository repo) {
        return new InMemoryOAuth2AuthorizedClientService(repo);
    }
}
