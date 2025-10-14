package com.fawroo.iam.service;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final Keycloak keycloak;

    // CREATE REALM ROLE
    public void createRealmRole(String realmName, RoleRepresentation roleRepresentation) {
        try {
            keycloak.realms().realm(realmName).roles().create(roleRepresentation);
            log.info("Realm role created successfully: {} in realm {}", roleRepresentation.getName(), realmName);
        } catch (Exception e) {
            log.error("Error creating realm role: {}", e.getMessage());
            throw new RuntimeException("Failed to create realm role: " + e.getMessage());
        }
    }
    
    // GET ALL REALM ROLES
    public List<RoleRepresentation> getAllRealmRoles(String realmName) {
        try {
            return keycloak.realms().realm(realmName).roles().list();
        } catch (Exception e) {
            log.error("Error getting realm roles for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get realm roles: " + e.getMessage());
        }
    }
    
    // GET REALM ROLE BY NAME
    public RoleRepresentation getRealmRole(String realmName, String roleName) {
        try {
            return keycloak.realms().realm(realmName).roles().get(roleName).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting realm role {}: {}", roleName, e.getMessage());
            throw new RuntimeException("Role not found: " + e.getMessage());
        }
    }
    
    // UPDATE REALM ROLE
    public void updateRealmRole(String realmName, String roleName, RoleRepresentation roleRepresentation) {
        try {
            keycloak.realms().realm(realmName).roles().get(roleName).update(roleRepresentation);
            log.info("Realm role updated successfully: {} in realm {}", roleName, realmName);
        } catch (Exception e) {
            log.error("Error updating realm role {}: {}", roleName, e.getMessage());
            throw new RuntimeException("Failed to update realm role: " + e.getMessage());
        }
    }
    
    // DELETE REALM ROLE
    public void deleteRealmRole(String realmName, String roleName) {
        try {
            keycloak.realms().realm(realmName).roles().deleteRole(roleName);
            log.info("Realm role deleted successfully: {} from realm {}", roleName, realmName);
        } catch (Exception e) {
            log.error("Error deleting realm role {}: {}", roleName, e.getMessage());
            throw new RuntimeException("Failed to delete realm role: " + e.getMessage());
        }
    }
    
    // CREATE CLIENT ROLE
    public void createClientRole(String realmName, String clientId, RoleRepresentation roleRepresentation) {
        try {
            String clientUuid = getClientUuid(realmName, clientId);
            keycloak.realms().realm(realmName).clients().get(clientUuid).roles().create(roleRepresentation);
            log.info("Client role created successfully: {} for client {} in realm {}", 
                    roleRepresentation.getName(), clientId, realmName);
        } catch (Exception e) {
            log.error("Error creating client role: {}", e.getMessage());
            throw new RuntimeException("Failed to create client role: " + e.getMessage());
        }
    }
    
    // GET ALL CLIENT ROLES
    public List<RoleRepresentation> getAllClientRoles(String realmName, String clientId) {
        try {
            String clientUuid = getClientUuid(realmName, clientId);
            return keycloak.realms().realm(realmName).clients().get(clientUuid).roles().list();
        } catch (Exception e) {
            log.error("Error getting client roles for client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to get client roles: " + e.getMessage());
        }
    }
    
    private String getClientUuid(String realmName, String clientId) {
        List<org.keycloak.representations.idm.ClientRepresentation> clients = keycloak.realms().realm(realmName).clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new RuntimeException("Client not found: " + clientId);
        }
        return clients.get(0).getId();
    }
}