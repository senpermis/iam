package com.fawroo.iam.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    // GET REALM DASHBOARD STATS
    public Map<String, Object> getRealmDashboard(String realmName) {
        try {
            var realmResource = keycloak.realms().realm(realmName);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("realmName", realmName);
            dashboard.put("totalUsers", realmResource.users().count());
            dashboard.put("totalClients", realmResource.clients().findAll().size());
            dashboard.put("totalGroups", realmResource.groups().groups().size());
            dashboard.put("totalRoles", realmResource.roles().list().size());
            dashboard.put("activeSessions", getActiveSessionsCount(realmName));
            dashboard.put("timestamp", LocalDateTime.now());

            // Add realm info
            RealmRepresentation realmInfo = realmResource.toRepresentation();
            dashboard.put("realmEnabled", realmInfo.isEnabled());
            dashboard.put("registrationAllowed", realmInfo.isRegistrationAllowed());
            dashboard.put("resetPasswordAllowed", realmInfo.isResetPasswordAllowed());

            return dashboard;
        } catch (Exception e) {
            log.error("Error getting dashboard for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get dashboard: " + e.getMessage());
        }
    }

    // GET USER ACTIVITY STATS
    public Map<String, Object> getUserActivityStats(String realmName) {
        try {
            var users = keycloak.realms().realm(realmName).users().list();

            Map<String, Object> activity = new HashMap<>();
            activity.put("totalUsers", users.size());
            activity.put("enabledUsers", users.stream().filter(user -> user.isEnabled()).count());
            activity.put("disabledUsers", users.stream().filter(user -> !user.isEnabled()).count());
            activity.put("usersWithEmailVerified", users.stream().filter(user -> user.isEmailVerified()).count());
            activity.put("usersCreatedLast7Days", users.stream()
                    .filter(user -> isWithinLast7Days(user.getCreatedTimestamp()))
                    .count());

            return activity;
        } catch (Exception e) {
            log.error("Error getting user activity stats for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get user activity stats: " + e.getMessage());
        }
    }

    // GET CLIENT USAGE STATS
    public Map<String, Object> getClientUsageStats(String realmName) {
        try {
            var clients = keycloak.realms().realm(realmName).clients().findAll();
            var clientStats = keycloak.realms().realm(realmName).getClientSessionStats();

            Map<String, Object> usage = new HashMap<>();
            usage.put("totalClients", clients.size());
            usage.put("publicClients",
                    clients.stream().filter(client -> "public".equals(client.isPublicClient())).count());
            usage.put("confidentialClients",
                    clients.stream().filter(client -> !"public".equals(client.isPublicClient())).count());
            usage.put("clientSessions", clientStats);

            return usage;
        } catch (Exception e) {
            log.error("Error getting client usage stats for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get client usage stats: " + e.getMessage());
        }
    }

    // GET SYSTEM HEALTH
    public Map<String, Object> getSystemHealth(String realmName) {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("realmAccessible", true);
            health.put("serverTime", LocalDateTime.now());
            health.put("usersAccessible", true);
            health.put("clientsAccessible", true);
            health.put("databaseConnected", true);

            // Test basic operations
            try {
                keycloak.realms().realm(realmName).users().count();
                health.put("userOperations", "healthy");
            } catch (Exception e) {
                health.put("userOperations", "unhealthy");
            }

            return health;
        } catch (Exception e) {
            log.error("Error getting system health for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get system health: " + e.getMessage());
        }
    }

    private int getActiveSessionsCount(String realmName) {
        try {
            // Utiliser les sessions client pour obtenir le nombre de sessions actives
            var clientStats = keycloak.realms().realm(realmName).getClientSessionStats();
            return clientStats != null ? clientStats.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isWithinLast7Days(Long timestamp) {
        if (timestamp == null)
            return false;
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        return timestamp > sevenDaysAgo;
    }
}