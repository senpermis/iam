package com.fawroo.iam.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {

    private final Keycloak keycloak;

    // SET PASSWORD POLICY
    public void setPasswordPolicy(String realmName, String policy) {
        try {
            RealmRepresentation realm = keycloak.realms().realm(realmName).toRepresentation();
            realm.setPasswordPolicy(policy);
            keycloak.realms().realm(realmName).update(realm);
            log.info("Password policy set successfully for realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error setting password policy for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to set password policy: " + e.getMessage());
        }
    }

    // GET PASSWORD POLICY TYPES
    public List<String> getPasswordPolicyTypes(String realmName) {
        try {
            RealmRepresentation realm = keycloak.realms().realm(realmName).toRepresentation();
            String passwordPolicy = realm.getPasswordPolicy();
            if (passwordPolicy != null) {
                return List.of(passwordPolicy.split(" and "));
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error getting password policy types for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get password policy types: " + e.getMessage());
        }
    }

    // CONFIGURE BRUTE FORCE PROTECTION
    public void configureBruteForceProtection(String realmName, Map<String, String> config) {
        try {
            RealmRepresentation realm = keycloak.realms().realm(realmName).toRepresentation();
            realm.setBruteForceProtected(Boolean.parseBoolean(config.get("enabled")));
            realm.setFailureFactor(Integer.parseInt(config.getOrDefault("failureFactor", "30")));
            realm.setWaitIncrementSeconds(Integer.parseInt(config.getOrDefault("waitIncrementSeconds", "60")));
            keycloak.realms().realm(realmName).update(realm);
            log.info("Brute force protection configured for realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error configuring brute force protection for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to configure brute force protection: " + e.getMessage());
        }
    }

    // CLEAR BRUTE FORCE PROTECTION FOR USER
    public void clearBruteForceForUser(String realmName, String userId) {
        try {
            keycloak.realms().realm(realmName).attackDetection().clearBruteForceForUser(userId);
            log.info("Brute force protection cleared for user: {}", userId);
        } catch (Exception e) {
            log.error("Error clearing brute force protection for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to clear brute force protection: " + e.getMessage());
        }
    }

    // GET USER LOGIN FAILURES
    public Map<String, Object> getUserLoginFailures(String realmName, String userId) {
        try {
            var attackDetection = keycloak.realms().realm(realmName).attackDetection();
            var status = attackDetection.bruteForceUserStatus(userId);

            // Convertir en Map standard
            Map<String, Object> result = new HashMap<>();
            if (status != null) {
                result.put("numFailures", status.get("numFailures"));
                result.put("disabled", status.get("disabled"));
                result.put("lastIPFailure", status.get("lastIPFailure"));
                result.put("lastFailure", status.get("lastFailure"));
            }
            return result;
        } catch (Exception e) {
            log.error("Error getting login failures for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user login failures: " + e.getMessage());
        }
    }

    // CONFIGURE SSL/TLS
    public void configureSSL(String realmName, Map<String, String> sslConfig) {
        try {
            RealmRepresentation realm = keycloak.realms().realm(realmName).toRepresentation();
            realm.setSslRequired(sslConfig.get("sslRequired"));
            keycloak.realms().realm(realmName).update(realm);
            log.info("SSL configuration updated for realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error configuring SSL for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to configure SSL: " + e.getMessage());
        }
    }

    // ADD SECURITY COMPONENT (ex: LDAP, Kerberos)
    public void addSecurityComponent(String realmName, ComponentRepresentation component) {
        try {
            keycloak.realms().realm(realmName).components().add(component);
            log.info("Security component added to realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error adding security component to {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to add security component: " + e.getMessage());
        }
    }
}