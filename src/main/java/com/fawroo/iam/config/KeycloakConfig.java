package com.fawroo.iam.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.username}")
    private String username;
    
    @Value("${keycloak.password}")
    private String password;
    
    @Value("${keycloak.client-id}")
    private String clientId;
    
    // @Value("${keycloak.client-secret}")
    // private String clientSecret;
    
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                // .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .build();
    }
}