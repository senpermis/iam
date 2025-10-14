package com.fawroo.iam.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final Keycloak keycloak;

    // CREATE GROUP
    public String createGroup(String realmName, GroupRepresentation groupRepresentation) {
        try {
            Response response = keycloak.realms().realm(realmName).groups().add(groupRepresentation);
            
            if (response.getStatus() == 201) {
                String groupId = extractGroupIdFromLocation(response.getLocation());
                log.info("Group created successfully: {} in realm {}", groupRepresentation.getName(), realmName);
                return groupId;
            } else {
                throw new RuntimeException("Failed to create group. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error creating group: {}", e.getMessage());
            throw new RuntimeException("Failed to create group: " + e.getMessage());
        }
    }
    
    // GET ALL GROUPS
    public List<GroupRepresentation> getAllGroups(String realmName) {
        try {
            return keycloak.realms().realm(realmName).groups().groups();
        } catch (Exception e) {
            log.error("Error getting groups for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get groups: " + e.getMessage());
        }
    }
    
    // GET GROUP BY ID
    public GroupRepresentation getGroup(String realmName, String groupId) {
        try {
            return keycloak.realms().realm(realmName).groups().group(groupId).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Group not found: " + e.getMessage());
        }
    }
    
    // GET GROUP BY NAME/PATH
    public GroupRepresentation getGroupByPath(String realmName, String path) {
        try {
            return keycloak.realms().realm(realmName).getGroupByPath(path);
        } catch (Exception e) {
            log.error("Error getting group by path {}: {}", path, e.getMessage());
            throw new RuntimeException("Group not found: " + e.getMessage());
        }
    }
    
    // UPDATE GROUP
    public void updateGroup(String realmName, String groupId, GroupRepresentation groupRepresentation) {
        try {
            groupRepresentation.setId(groupId);
            keycloak.realms().realm(realmName).groups().group(groupId).update(groupRepresentation);
            log.info("Group updated successfully: {} in realm {}", groupRepresentation.getName(), realmName);
        } catch (Exception e) {
            log.error("Error updating group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to update group: " + e.getMessage());
        }
    }
    
    // DELETE GROUP
    public void deleteGroup(String realmName, String groupId) {
        try {
            keycloak.realms().realm(realmName).groups().group(groupId).remove();
            log.info("Group deleted successfully: {} from realm {}", groupId, realmName);
        } catch (Exception e) {
            log.error("Error deleting group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to delete group: " + e.getMessage());
        }
    }
    
    // GET GROUP MEMBERS
    public List<UserRepresentation> getGroupMembers(String realmName, String groupId) {
        try {
            return keycloak.realms().realm(realmName).groups().group(groupId).members();
        } catch (Exception e) {
            log.error("Error getting group members for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to get group members: " + e.getMessage());
        }
    }
    
    // GET GROUP ROLES
    public List<RoleRepresentation> getGroupRealmRoles(String realmName, String groupId) {
        try {
            return keycloak.realms().realm(realmName).groups().group(groupId).roles().realmLevel().listAll();
        } catch (Exception e) {
            log.error("Error getting group roles for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to get group roles: " + e.getMessage());
        }
    }
    
    // ASSIGN ROLE TO GROUP
    public void assignRoleToGroup(String realmName, String groupId, RoleRepresentation role) {
        try {
            List<RoleRepresentation> roles = new ArrayList<>();
            roles.add(role);
            keycloak.realms().realm(realmName).groups().group(groupId).roles().realmLevel().add(roles);
            log.info("Role {} assigned to group {}", role.getName(), groupId);
        } catch (Exception e) {
            log.error("Error assigning role to group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to assign role to group: " + e.getMessage());
        }
    }
    
    private String extractGroupIdFromLocation(URI location) {
        if (location != null) {
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
        throw new RuntimeException("Could not extract group ID from response");
    }
}