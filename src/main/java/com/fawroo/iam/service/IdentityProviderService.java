package com.fawroo.iam.service;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdentityProviderService {

    private final Keycloak keycloak;

    // CREATE IDENTITY PROVIDER
    public void createIdentityProvider(String realmName, IdentityProviderRepresentation provider) {
        try {
            keycloak.realms().realm(realmName).identityProviders().create(provider);
            log.info("Identity provider created successfully: {} in realm {}", provider.getAlias(), realmName);
        } catch (Exception e) {
            log.error("Error creating identity provider: {}", e.getMessage());
            throw new RuntimeException("Failed to create identity provider: " + e.getMessage());
        }
    }

    // GET ALL IDENTITY PROVIDERS
    public List<IdentityProviderRepresentation> getAllIdentityProviders(String realmName) {
        try {
            return keycloak.realms().realm(realmName).identityProviders().findAll();
        } catch (Exception e) {
            log.error("Error getting identity providers for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get identity providers: " + e.getMessage());
        }
    }

    // GET IDENTITY PROVIDER BY ALIAS
    public IdentityProviderRepresentation getIdentityProvider(String realmName, String alias) {
        try {
            return keycloak.realms().realm(realmName).identityProviders().get(alias).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting identity provider {}: {}", alias, e.getMessage());
            throw new RuntimeException("Identity provider not found: " + e.getMessage());
        }
    }

    // UPDATE IDENTITY PROVIDER
    public void updateIdentityProvider(String realmName, String alias, IdentityProviderRepresentation provider) {
        try {
            keycloak.realms().realm(realmName).identityProviders().get(alias).update(provider);
            log.info("Identity provider updated successfully: {} in realm {}", alias, realmName);
        } catch (Exception e) {
            log.error("Error updating identity provider {}: {}", alias, e.getMessage());
            throw new RuntimeException("Failed to update identity provider: " + e.getMessage());
        }
    }

    // DELETE IDENTITY PROVIDER
    public void deleteIdentityProvider(String realmName, String alias) {
        try {
            keycloak.realms().realm(realmName).identityProviders().get(alias).remove();
            log.info("Identity provider deleted successfully: {} from realm {}", alias, realmName);
        } catch (Exception e) {
            log.error("Error deleting identity provider {}: {}", alias, e.getMessage());
            throw new RuntimeException("Failed to delete identity provider: " + e.getMessage());
        }
    }
}