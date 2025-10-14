package com.fawroo.iam.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealmService {

    private final Keycloak keycloak;

    // CREATE REALM
    public void createRealm(RealmRepresentation realmRepresentation) {
        try {
            keycloak.realms().create(realmRepresentation);
            log.info("Realm created successfully: {}", realmRepresentation.getRealm());
        } catch (Exception e) {
            log.error("Error creating realm: {}", e.getMessage());
            throw new RuntimeException("Failed to create realm: " + e.getMessage());
        }
    }

    // GET ALL REALMS
    public List<RealmRepresentation> getAllRealms() {
        try {
            return keycloak.realms().findAll();
        } catch (Exception e) {
            log.error("Error getting all realms: {}", e.getMessage());
            throw new RuntimeException("Failed to get realms: " + e.getMessage());
        }
    }

    // GET REALM BY NAME
    public RealmRepresentation getRealm(String realmName) {
        try {
            return keycloak.realms().realm(realmName).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Realm not found: " + e.getMessage());
        }
    }

    // UPDATE REALM
    public void updateRealm(String realmName, RealmRepresentation realmRepresentation) {
        try {
            realmRepresentation.setRealm(realmName);
            keycloak.realms().realm(realmName).update(realmRepresentation);
            log.info("Realm updated successfully: {}", realmName);
        } catch (Exception e) {
            log.error("Error updating realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to update realm: " + e.getMessage());
        }
    }

    // DELETE REALM
    public void deleteRealm(String realmName) {
        try {
            keycloak.realms().realm(realmName).remove();
            log.info("Realm deleted successfully: {}", realmName);
        } catch (Exception e) {
            log.error("Error deleting realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to delete realm: " + e.getMessage());
        }
    }

    // REALM EXISTS
    public boolean realmExists(String realmName) {
        try {
            keycloak.realms().realm(realmName).toRepresentation();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // GET ALL USERS IN REALM
    public List<UserRepresentation> getRealmUsers(String realmName) {
        try {
            return keycloak.realms().realm(realmName).users().list();
        } catch (Exception e) {
            log.error("Error getting users for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get realm users: " + e.getMessage());
        }
    }

    // GET ALL USERS IN REALM WITH PAGINATION
    public List<UserRepresentation> getRealmUsers(String realmName, int first, int max) {
        try {
            return keycloak.realms().realm(realmName).users().list(first, max);
        } catch (Exception e) {
            log.error("Error getting users for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get realm users: " + e.getMessage());
        }
    }

    // GET USERS COUNT
    public Integer getRealmUsersCount(String realmName) {
        try {
            return keycloak.realms().realm(realmName).users().count();
        } catch (Exception e) {
            log.error("Error getting users count for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get users count: " + e.getMessage());
        }
    }

    // GET REALM STATISTICS
    public Map<String, Object> getRealmStatistics(String realmName) {
        try {
            RealmResource realmResource = keycloak.realms().realm(realmName);

            Map<String, Object> stats = new HashMap<>();
            stats.put("realmName", realmName);
            stats.put("usersCount", realmResource.users().count());
            stats.put("groupsCount", realmResource.groups().groups().size());
            stats.put("rolesCount", realmResource.roles().list().size());
            stats.put("clientsCount", realmResource.clients().findAll().size());
            stats.put("timestamp", java.time.LocalDateTime.now());

            return stats;
        } catch (Exception e) {
            log.error("Error getting realm statistics for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get realm statistics: " + e.getMessage());
        }
    }
}