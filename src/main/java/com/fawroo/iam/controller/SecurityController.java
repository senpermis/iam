package com.fawroo.iam.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fawroo.iam.service.SecurityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/realms/{realmName}/security")
@RequiredArgsConstructor
@Slf4j
public class SecurityController {

    private final SecurityService securityService;

    // ========== PASSWORD POLICY ENDPOINTS ==========

    @PutMapping("/password-policy")
    public ResponseEntity<Void> setPasswordPolicy(
            @PathVariable String realmName,
            @RequestBody Map<String, String> policyRequest) {
        String policy = policyRequest.get("policy");
        securityService.setPasswordPolicy(realmName, policy);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/password-policy")
    public ResponseEntity<List<String>> getPasswordPolicyTypes(@PathVariable String realmName) {
        List<String> policyTypes = securityService.getPasswordPolicyTypes(realmName);
        return ResponseEntity.ok(policyTypes);
    }

    // ========== BRUTE FORCE PROTECTION ENDPOINTS ==========

    @PutMapping("/brute-force-protection")
    public ResponseEntity<Void> configureBruteForceProtection(
            @PathVariable String realmName,
            @RequestBody Map<String, String> config) {
        securityService.configureBruteForceProtection(realmName, config);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/brute-force-protection/users/{userId}")
    public ResponseEntity<Void> clearBruteForceForUser(
            @PathVariable String realmName,
            @PathVariable String userId) {
        securityService.clearBruteForceForUser(realmName, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/brute-force-protection/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserLoginFailures(
            @PathVariable String realmName,
            @PathVariable String userId) {
        Map<String, Object> loginFailures = securityService.getUserLoginFailures(realmName, userId);
        return ResponseEntity.ok(loginFailures);
    }

    // ========== SSL/TLS CONFIGURATION ENDPOINTS ==========

    @PutMapping("/ssl")
    public ResponseEntity<Void> configureSSL(
            @PathVariable String realmName,
            @RequestBody Map<String, String> sslConfig) {
        securityService.configureSSL(realmName, sslConfig);
        return ResponseEntity.ok().build();
    }

    // ========== SECURITY COMPONENTS ENDPOINTS ==========

    @PostMapping("/components")
    public ResponseEntity<Void> addSecurityComponent(
            @PathVariable String realmName,
            @RequestBody ComponentRepresentation component) {
        securityService.addSecurityComponent(realmName, component);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ========== SECURITY STATUS ENDPOINTS ==========

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSecurityStatus(@PathVariable String realmName) {
        try {
            // Récupérer les informations de sécurité actuelles
            List<String> passwordPolicy = securityService.getPasswordPolicyTypes(realmName);
            
            Map<String, Object> status = Map.of(
                "realm", realmName,
                "passwordPolicy", passwordPolicy,
                "bruteForceProtection", getBruteForceProtectionStatus(realmName),
                "sslConfigured", isSSLConfigured(realmName),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // ========== BULK SECURITY OPERATIONS ==========

    @PostMapping("/bulk/clear-brute-force")
    public ResponseEntity<Map<String, String>> clearBruteForceForMultipleUsers(
            @PathVariable String realmName,
            @RequestBody List<String> userIds) {
        try {
            int clearedCount = 0;
            for (String userId : userIds) {
                try {
                    securityService.clearBruteForceForUser(realmName, userId);
                    clearedCount++;
                } catch (Exception e) {
                    log.warn("Failed to clear brute force for user {}: {}", userId, e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Collections.singletonMap("clearedUsers", clearedCount+ ""));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    private Map<String, Object> getBruteForceProtectionStatus(String realmName) {
        try {
            // Implémentation pour récupérer le statut de la protection brute force
            return Map.of(
                "enabled", true, // À implémenter selon vos besoins
                "maxLoginFailures", 30,
                "waitIncrementSeconds", 60
            );
        } catch (Exception e) {
            return Map.of("enabled", false, "error", e.getMessage());
        }
    }

    private boolean isSSLConfigured(String realmName) {
        try {
            // Implémentation pour vérifier si SSL est configuré
            return true; // À implémenter selon vos besoins
        } catch (Exception e) {
            return false;
        }
    }
}