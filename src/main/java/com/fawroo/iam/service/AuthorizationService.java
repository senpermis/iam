package com.fawroo.iam.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {

    private final Keycloak keycloak;

    // ENABLE AUTHORIZATION SERVICES FOR CLIENT
    public void enableAuthorizationServices(String realmName, String clientId) {
        try {
            var client = keycloak.realms().realm(realmName).clients().findByClientId(clientId).get(0);
            var authzSettings = keycloak.realms().realm(realmName).clients().get(client.getId()).authorization();

            ResourceServerRepresentation settings = new ResourceServerRepresentation();
            settings.setAllowRemoteResourceManagement(true);
            settings.setPolicyEnforcementMode(
                    org.keycloak.representations.idm.authorization.PolicyEnforcementMode.ENFORCING);

            authzSettings.update(settings);
            log.info("Authorization services enabled for client: {}", clientId);
        } catch (Exception e) {
            log.error("Error enabling authorization services for client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to enable authorization services: " + e.getMessage());
        }
    }

    // CREATE RESOURCE
    public void createResource(String realmName, String clientId, ResourceRepresentation resource) {
        try {
            var client = keycloak.realms().realm(realmName).clients().findByClientId(clientId).get(0);
            keycloak.realms().realm(realmName).clients().get(client.getId()).authorization().resources()
                    .create(resource);
            log.info("Resource created successfully for client: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating resource for client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to create resource: " + e.getMessage());
        }
    }

    // CREATE POLICY
    public void createPolicy(String realmName, String clientId, PolicyRepresentation policy) {
        try {
            var client = keycloak.realms().realm(realmName).clients().findByClientId(clientId).get(0);
            keycloak.realms().realm(realmName).clients().get(client.getId()).authorization().policies().create(policy);
            log.info("Policy created successfully for client: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating policy for client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to create policy: " + e.getMessage());
        }
    }

    // CREATE SCOPE
    public void createScope(String realmName, String clientId, ScopeRepresentation scope) {
        try {
            var client = keycloak.realms().realm(realmName).clients().findByClientId(clientId).get(0);
            keycloak.realms().realm(realmName).clients().get(client.getId()).authorization().scopes().create(scope);
            log.info("Scope created successfully for client: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating scope for client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to create scope: " + e.getMessage());
        }
    }

    // GET AUTHORIZATION PERMISSIONS
    public Map<String, Object> getAuthorizationPermissions(String realmName, String userId) {
        try {
            var userResource = keycloak.realms().realm(realmName).users().get(userId);
            // Récupérer les rôles et groupes de l'utilisateur comme proxy des permissions
            var roles = userResource.roles().realmLevel().listAll();
            var groups = userResource.groups();

            Map<String, Object> permissions = new HashMap<>();
            permissions.put("roles", roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            permissions.put("groups", groups.stream().map(GroupRepresentation::getName).collect(Collectors.toList()));
            permissions.put("userId", userId);

            log.info("Retrieved authorization permissions for user: {}", userId);
            return permissions;
        } catch (Exception e) {
            log.error("Error getting authorization permissions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get authorization permissions: " + e.getMessage());
        }
    }
}