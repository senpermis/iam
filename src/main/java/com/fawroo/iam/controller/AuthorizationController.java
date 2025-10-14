package com.fawroo.iam.controller;

import com.fawroo.iam.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/realms/{realmName}/authz")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    @PostMapping("/clients/{clientId}/enable")
    public ResponseEntity<String> enableAuthorizationServices(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        authorizationService.enableAuthorizationServices(realmName, clientId);
        return ResponseEntity.ok("Authorization services enabled successfully");
    }

    @PostMapping("/clients/{clientId}/resources")
    public ResponseEntity<String> createResource(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ResourceRepresentation resource) {
        authorizationService.createResource(realmName, clientId, resource);
        return ResponseEntity.ok("Resource created successfully");
    }

    @PostMapping("/clients/{clientId}/policies")
    public ResponseEntity<String> createPolicy(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody PolicyRepresentation policy) {
        authorizationService.createPolicy(realmName, clientId, policy);
        return ResponseEntity.ok("Policy created successfully");
    }

    @PostMapping("/clients/{clientId}/scopes")
    public ResponseEntity<String> createScope(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ScopeRepresentation scope) {
        authorizationService.createScope(realmName, clientId, scope);
        return ResponseEntity.ok("Scope created successfully");
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<Map<String, Object>> getAuthorizationPermissions(
            @PathVariable String realmName,
            @PathVariable String userId) {
        Map<String, Object> permissions = authorizationService.getAuthorizationPermissions(realmName, userId);
        return ResponseEntity.ok(permissions);
    }
}