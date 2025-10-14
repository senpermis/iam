package com.fawroo.iam.service;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionService {

    private final Keycloak keycloak;
    
    @Value("${keycloak.realm}")
    private String realm;

    // GET USER SESSIONS
    public List<UserSessionRepresentation> getUserSessions(String userId) {
        try {
            return keycloak.realm(realm).users().get(userId).getUserSessions();
        } catch (Exception e) {
            log.error("Error getting sessions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user sessions: " + e.getMessage());
        }
    }

    // GET OFFLINE SESSIONS
    public List<UserSessionRepresentation> getOfflineSessions(String userId, String clientId) {
        try {
            return keycloak.realm(realm).users().get(userId).getOfflineSessions(clientId);
        } catch (Exception e) {
            log.error("Error getting offline sessions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get offline sessions: " + e.getMessage());
        }
    }

    // LOGOUT USER (invalidate all sessions)
    public void logoutUser(String userId) {
        try {
            keycloak.realm(realm).users().get(userId).logout();
            log.info("User logged out successfully: {}", userId);
        } catch (Exception e) {
            log.error("Error logging out user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to logout user: " + e.getMessage());
        }
    }
}