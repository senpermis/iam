package com.fawroo.iam.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fawroo.iam.model.dto.Credential;
import com.fawroo.iam.model.dto.PasswordUpdateRequest;
import com.fawroo.iam.model.dto.UserProfile;
import com.fawroo.iam.model.dto.UserRequest;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KeycloakService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakService(Keycloak keycloak, @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = "senpermis"; // realm;
    }
    
    // ========== REALM MANAGEMENT ==========
    
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
    
    // ========== ROLE MANAGEMENT ==========
    
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
    
    // ========== GROUP MANAGEMENT ==========
    
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
    
    // ========== HELPER METHODS ==========
    
    private String getClientUuid(String realmName, String clientId) {
        List<ClientRepresentation> clients = keycloak.realms().realm(realmName).clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new RuntimeException("Client not found: " + clientId);
        }
        return clients.get(0).getId();
    }
    
    private String extractGroupIdFromLocation(URI location) {
        if (location != null) {
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
        throw new RuntimeException("Could not extract group ID from response");
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

    // CREATE USER
    public String createUser(UserRequest userRequest) {
        try {
            UsersResource usersResource = keycloak.realm(realm).users();
            UserRepresentation user = mapToUserRepresentation(userRequest);

            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromLocation(response.getLocation());
                log.info("User created successfully with ID: {}", userId);
                return userId;
            } else {
                throw new RuntimeException("Failed to create user. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    // GET USER BY ID
    public UserRepresentation getUserById(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.toRepresentation();
        } catch (Exception e) {
            log.error("Error getting user by ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("User not found: " + e.getMessage());
        }
    }

    // GET USER BY USERNAME
    public UserRepresentation getUserByUsername(String username) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users()
                    .search(username, 0, 1);

            if (users.isEmpty()) {
                throw new RuntimeException("User not found with username: " + username);
            }

            return users.get(0);
        } catch (Exception e) {
            log.error("Error getting user by username {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to get user: " + e.getMessage());
        }
    }

    // GET ALL USERS
    public List<UserRepresentation> getAllUsers() {
        try {
            return keycloak.realm(realm).users().list();
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage());
            throw new RuntimeException("Failed to get users: " + e.getMessage());
        }
    }

    // SEARCH USERS
    public List<UserRepresentation> searchUsers(String search, int first, int max) {
        try {
            return keycloak.realm(realm).users().search(search, first, max);
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage());
            throw new RuntimeException("Failed to search users: " + e.getMessage());
        }
    }

    // UPDATE USER
    public void updateUser(String userId, UserRequest userRequest) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            UserRepresentation user = mapToUserRepresentation(userRequest);
            user.setId(userId);

            userResource.update(user);
            log.info("User updated successfully: {}", userId);
        } catch (Exception e) {
            log.error("Error updating user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    // DELETE USER
    public void deleteUser(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.remove();
            log.info("User deleted successfully: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    // UPDATE PASSWORD
    public void updatePassword(String userId, PasswordUpdateRequest passwordRequest) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(passwordRequest.getNewPassword());
            credential.setTemporary(passwordRequest.isTemporary());

            userResource.resetPassword(credential);
            log.info("Password updated for user: {}", userId);
        } catch (Exception e) {
            log.error("Error updating password for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update password: " + e.getMessage());
        }
    }

    // GET USER SESSIONS
    public List<UserSessionRepresentation> getUserSessions(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.getUserSessions();
        } catch (Exception e) {
            log.error("Error getting sessions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user sessions: " + e.getMessage());
        }
    }

    // LOGOUT USER (invalidate all sessions)
    public void logoutUser(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.logout();
            log.info("User logged out successfully: {}", userId);
        } catch (Exception e) {
            log.error("Error logging out user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to logout user: " + e.getMessage());
        }
    }

    // GET USER PROFILE
    public UserProfile getUserProfile(String userId) {
        try {
            UserRepresentation user = getUserById(userId);
            return mapToUserProfile(user);
        } catch (Exception e) {
            log.error("Error getting profile for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user profile: " + e.getMessage());
        }
    }

    // ENABLE/DISABLE USER
    public void setUserEnabled(String userId, boolean enabled) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(enabled);
            userResource.update(user);
            log.info("User {} enabled: {}", userId, enabled);
        } catch (Exception e) {
            log.error("Error setting enabled status for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user status: " + e.getMessage());
        }
    }
    // KeycloakService.java - Ajoutez ces méthodes

    // COUNT USERS
    public Integer getUsersCount() {
        try {
            return keycloak.realm(realm).users().count();
        } catch (Exception e) {
            log.error("Error getting users count: {}", e.getMessage());
            throw new RuntimeException("Failed to get users count: " + e.getMessage());
        }
    }

    // GET USER GROUPS
    public List<GroupRepresentation> getUserGroups(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.groups();
        } catch (Exception e) {
            log.error("Error getting groups for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user groups: " + e.getMessage());
        }
    }

    // ADD USER TO GROUP
    public void addUserToGroup(String userId, String groupId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.joinGroup(groupId);
            log.info("User {} added to group {}", userId, groupId);
        } catch (Exception e) {
            log.error("Error adding user {} to group {}: {}", userId, groupId, e.getMessage());
            throw new RuntimeException("Failed to add user to group: " + e.getMessage());
        }
    }

    // REMOVE USER FROM GROUP
    public void removeUserFromGroup(String userId, String groupId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.leaveGroup(groupId);
            log.info("User {} removed from group {}", userId, groupId);
        } catch (Exception e) {
            log.error("Error removing user {} from group {}: {}", userId, groupId, e.getMessage());
            throw new RuntimeException("Failed to remove user from group: " + e.getMessage());
        }
    }

    // GET USER ROLES
    public List<RoleRepresentation> getUserRoles(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.roles().realmLevel().listAll();
        } catch (Exception e) {
            log.error("Error getting roles for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user roles: " + e.getMessage());
        }
    }

    // ASSIGN ROLE TO USER
    public void assignRoleToUser(String userId, RoleRepresentation role) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            List<RoleRepresentation> roles = new ArrayList<>();
            roles.add(role);
            userResource.roles().realmLevel().add(roles);
            log.info("Role {} assigned to user {}", role.getName(), userId);
        } catch (Exception e) {
            log.error("Error assigning role to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to assign role to user: " + e.getMessage());
        }
    }

    // REMOVE ROLE FROM USER
    public void removeRoleFromUser(String userId, RoleRepresentation role) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            List<RoleRepresentation> roles = new ArrayList<>();
            roles.add(role);
            userResource.roles().realmLevel().remove(roles);
            log.info("Role {} removed from user {}", role.getName(), userId);
        } catch (Exception e) {
            log.error("Error removing role from user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to remove role from user: " + e.getMessage());
        }
    }

    // GET USER CLIENT ROLES
    public List<RoleRepresentation> getUserClientRoles(String userId, String clientId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.roles().clientLevel(clientId).listAll();
        } catch (Exception e) {
            log.error("Error getting client roles for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user client roles: " + e.getMessage());
        }
    }

    // SEND VERIFICATION EMAIL
    public void sendVerificationEmail(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.sendVerifyEmail();
            log.info("Verification email sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending verification email to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    // SEND RESET PASSWORD EMAIL
    public void sendResetPassword(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));
            log.info("Reset password email sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending reset password email to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to send reset password email: " + e.getMessage());
        }
    }

    // GET USER CONSENTS
    public List<Map<String, Object>> getUserConsents(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            List<Map<String, Object>> consents = userResource.getConsents();
            log.info("Retrieved consents for user: {}", userId);
            return consents;
        } catch (Exception e) {
            log.error("Error getting consents for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user consents: " + e.getMessage());
        }
    }

    // REVOKE USER CONSENTS
    public void revokeUserConsents(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.revokeConsent(userId);
            log.info("All consents revoked for user: {}", userId);
        } catch (Exception e) {
            log.error("Error revoking consents for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to revoke user consents: " + e.getMessage());
        }
    }

    // GET OFFLINE SESSIONS
    public List<UserSessionRepresentation> getOfflineSessions(String userId, String clientId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.getOfflineSessions(clientId);
        } catch (Exception e) {
            log.error("Error getting offline sessions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get offline sessions: " + e.getMessage());
        }
    }

    // GET USER FEDERATED IDENTITY
    public List<FederatedIdentityRepresentation> getUserFederatedIdentity(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            return userResource.getFederatedIdentity();
        } catch (Exception e) {
            log.error("Error getting federated identity for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get federated identity: " + e.getMessage());
        }
    }

    // CHECK USER EXISTS
    public boolean userExists(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.toRepresentation(); // Will throw exception if user doesn't exist
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // BULK USER OPERATIONS - Disable multiple users
    public void bulkDisableUsers(List<String> userIds) {
        try {
            for (String userId : userIds) {
                setUserEnabled(userId, false);
            }
            log.info("Bulk disabled {} users", userIds.size());
        } catch (Exception e) {
            log.error("Error in bulk disable operation: {}", e.getMessage());
            throw new RuntimeException("Failed to bulk disable users: " + e.getMessage());
        }
    }

    // Helper methods
    private UserRepresentation mapToUserRepresentation(UserRequest userRequest) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(userRequest.isEnabled());
        user.setAttributes(userRequest.getAttributes());

        if (userRequest.getCredentials() != null) {
            List<CredentialRepresentation> credentials = userRequest.getCredentials().stream()
                    .map(this::mapToCredentialRepresentation)
                    .collect(Collectors.toList());
            user.setCredentials(credentials);
        }

        return user;
    }

    private CredentialRepresentation mapToCredentialRepresentation(Credential credential) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(credential.getType());
        cred.setValue(credential.getValue());
        cred.setTemporary(credential.isTemporary());
        return cred;
    }

    private UserProfile mapToUserProfile(UserRepresentation user) {
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEnabled(user.isEnabled());
        profile.setAttributes(user.getAttributes());
        return profile;
    }

    private String extractUserIdFromLocation(URI location) {
        if (location != null) {
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
        throw new RuntimeException("Could not extract user ID from response");
    }
}