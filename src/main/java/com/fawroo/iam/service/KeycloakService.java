package com.fawroo.iam.service;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
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
        this.realm = "senpermis"; //realm;
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