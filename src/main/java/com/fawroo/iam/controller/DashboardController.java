package com.fawroo.iam.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fawroo.iam.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/realms/{realmName}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ========== DASHBOARD OVERVIEW ENDPOINTS ==========

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getRealmDashboard(@PathVariable String realmName) {
        Map<String, Object> dashboard = dashboardService.getRealmDashboard(realmName);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivityStats(@PathVariable String realmName) {
        Map<String, Object> activityStats = dashboardService.getUserActivityStats(realmName);
        return ResponseEntity.ok(activityStats);
    }

    @GetMapping("/client-usage")
    public ResponseEntity<Map<String, Object>> getClientUsageStats(@PathVariable String realmName) {
        Map<String, Object> usageStats = dashboardService.getClientUsageStats(realmName);
        return ResponseEntity.ok(usageStats);
    }

    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth(@PathVariable String realmName) {
        Map<String, Object> healthStatus = dashboardService.getSystemHealth(realmName);
        return ResponseEntity.ok(healthStatus);
    }

    // ========== COMPREHENSIVE DASHBOARD ENDPOINT ==========

    @GetMapping("/comprehensive")
    public ResponseEntity<ComprehensiveDashboardResponse> getComprehensiveDashboard(@PathVariable String realmName) {
        try {
            Map<String, Object> overview = dashboardService.getRealmDashboard(realmName);
            Map<String, Object> userActivity = dashboardService.getUserActivityStats(realmName);
            Map<String, Object> clientUsage = dashboardService.getClientUsageStats(realmName);
            Map<String, Object> systemHealth = dashboardService.getSystemHealth(realmName);

            ComprehensiveDashboardResponse response = ComprehensiveDashboardResponse.builder()
                    .realmName(realmName)
                    .overview(overview)
                    .userActivity(userActivity)
                    .clientUsage(clientUsage)
                    .systemHealth(systemHealth)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== REALM COMPARISON ENDPOINT ==========

    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getRealmsComparison() {
        try {
            // Cette méthode pourrait être étendue pour comparer plusieurs realms
            Map<String, Object> comparison = Map.of(
                "message", "Realm comparison feature - to be implemented",
                "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== DTOs ==========

    public static class ComprehensiveDashboardResponse {
        private String realmName;
        private Map<String, Object> overview;
        private Map<String, Object> userActivity;
        private Map<String, Object> clientUsage;
        private Map<String, Object> systemHealth;
        private java.time.LocalDateTime timestamp;

        // Getters and Setters
        public String getRealmName() { return realmName; }
        public void setRealmName(String realmName) { this.realmName = realmName; }
        public Map<String, Object> getOverview() { return overview; }
        public void setOverview(Map<String, Object> overview) { this.overview = overview; }
        public Map<String, Object> getUserActivity() { return userActivity; }
        public void setUserActivity(Map<String, Object> userActivity) { this.userActivity = userActivity; }
        public Map<String, Object> getClientUsage() { return clientUsage; }
        public void setClientUsage(Map<String, Object> clientUsage) { this.clientUsage = clientUsage; }
        public Map<String, Object> getSystemHealth() { return systemHealth; }
        public void setSystemHealth(Map<String, Object> systemHealth) { this.systemHealth = systemHealth; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }

        public static ComprehensiveDashboardResponseBuilder builder() {
            return new ComprehensiveDashboardResponseBuilder();
        }

        public static class ComprehensiveDashboardResponseBuilder {
            private ComprehensiveDashboardResponse response = new ComprehensiveDashboardResponse();

            public ComprehensiveDashboardResponseBuilder realmName(String realmName) {
                response.realmName = realmName;
                return this;
            }

            public ComprehensiveDashboardResponseBuilder overview(Map<String, Object> overview) {
                response.overview = overview;
                return this;
            }

            public ComprehensiveDashboardResponseBuilder userActivity(Map<String, Object> userActivity) {
                response.userActivity = userActivity;
                return this;
            }

            public ComprehensiveDashboardResponseBuilder clientUsage(Map<String, Object> clientUsage) {
                response.clientUsage = clientUsage;
                return this;
            }

            public ComprehensiveDashboardResponseBuilder systemHealth(Map<String, Object> systemHealth) {
                response.systemHealth = systemHealth;
                return this;
            }

            public ComprehensiveDashboardResponseBuilder timestamp(java.time.LocalDateTime timestamp) {
                response.timestamp = timestamp;
                return this;
            }

            public ComprehensiveDashboardResponse build() {
                return response;
            }
        }
    }
}