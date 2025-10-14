package com.fawroo.iam.controller;

import java.util.List;

import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fawroo.iam.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/realms/{realmName}/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    // ========== AUTHENTICATION FLOWS ENDPOINTS ==========

    @PostMapping("/flows")
    public ResponseEntity<Void> createAuthenticationFlow(
            @PathVariable String realmName,
            @RequestBody AuthenticationFlowRepresentation flow) {
        authenticationService.createAuthenticationFlow(realmName, flow);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/flows")
    public ResponseEntity<List<AuthenticationFlowRepresentation>> getAuthenticationFlows(
            @PathVariable String realmName) {
        List<AuthenticationFlowRepresentation> flows = authenticationService.getAuthenticationFlows(realmName);
        return ResponseEntity.ok(flows);
    }

    @PostMapping("/flows/{flowAlias}/executions")
    public ResponseEntity<Void> addExecutionToFlow(
            @PathVariable String realmName,
            @PathVariable String flowAlias,
            @RequestBody AuthenticationExecutionRepresentation execution) {
        authenticationService.addExecutionToFlow(realmName, flowAlias, execution);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ========== REQUIRED ACTIONS ENDPOINTS ==========

    @GetMapping("/required-actions")
    public ResponseEntity<List<RequiredActionProviderRepresentation>> getRequiredActions(
            @PathVariable String realmName) {
        List<RequiredActionProviderRepresentation> actions = authenticationService.getRequiredActions(realmName);
        return ResponseEntity.ok(actions);
    }

    @PutMapping("/required-actions/{alias}")
    public ResponseEntity<Void> updateRequiredAction(
            @PathVariable String realmName,
            @PathVariable String alias,
            @RequestBody RequiredActionProviderRepresentation action) {
        authenticationService.updateRequiredAction(realmName, alias, action);
        return ResponseEntity.ok().build();
    }

    // ========== AUTHENTICATION CONFIGURATION ENDPOINTS ==========

    @GetMapping("/config")
    public ResponseEntity<AuthenticationConfigResponse> getAuthenticationConfig(@PathVariable String realmName) {
        try {
            List<AuthenticationFlowRepresentation> flows = authenticationService.getAuthenticationFlows(realmName);
            List<RequiredActionProviderRepresentation> actions = authenticationService.getRequiredActions(realmName);

            AuthenticationConfigResponse config = AuthenticationConfigResponse.builder()
                    .realmName(realmName)
                    .authenticationFlows(flows)
                    .requiredActions(actions)
                    .totalFlows(flows.size())
                    .totalRequiredActions(actions.size())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== DTOs ==========

    public static class AuthenticationConfigResponse {
        private String realmName;
        private List<AuthenticationFlowRepresentation> authenticationFlows;
        private List<RequiredActionProviderRepresentation> requiredActions;
        private int totalFlows;
        private int totalRequiredActions;
        private java.time.LocalDateTime timestamp;

        // Getters and Setters
        public String getRealmName() { return realmName; }
        public void setRealmName(String realmName) { this.realmName = realmName; }
        public List<AuthenticationFlowRepresentation> getAuthenticationFlows() { return authenticationFlows; }
        public void setAuthenticationFlows(List<AuthenticationFlowRepresentation> authenticationFlows) { this.authenticationFlows = authenticationFlows; }
        public List<RequiredActionProviderRepresentation> getRequiredActions() { return requiredActions; }
        public void setRequiredActions(List<RequiredActionProviderRepresentation> requiredActions) { this.requiredActions = requiredActions; }
        public int getTotalFlows() { return totalFlows; }
        public void setTotalFlows(int totalFlows) { this.totalFlows = totalFlows; }
        public int getTotalRequiredActions() { return totalRequiredActions; }
        public void setTotalRequiredActions(int totalRequiredActions) { this.totalRequiredActions = totalRequiredActions; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }

        public static AuthenticationConfigResponseBuilder builder() {
            return new AuthenticationConfigResponseBuilder();
        }

        public static class AuthenticationConfigResponseBuilder {
            private AuthenticationConfigResponse response = new AuthenticationConfigResponse();

            public AuthenticationConfigResponseBuilder realmName(String realmName) {
                response.realmName = realmName;
                return this;
            }

            public AuthenticationConfigResponseBuilder authenticationFlows(List<AuthenticationFlowRepresentation> flows) {
                response.authenticationFlows = flows;
                return this;
            }

            public AuthenticationConfigResponseBuilder requiredActions(List<RequiredActionProviderRepresentation> actions) {
                response.requiredActions = actions;
                return this;
            }

            public AuthenticationConfigResponseBuilder totalFlows(int totalFlows) {
                response.totalFlows = totalFlows;
                return this;
            }

            public AuthenticationConfigResponseBuilder totalRequiredActions(int totalRequiredActions) {
                response.totalRequiredActions = totalRequiredActions;
                return this;
            }

            public AuthenticationConfigResponseBuilder timestamp(java.time.LocalDateTime timestamp) {
                response.timestamp = timestamp;
                return this;
            }

            public AuthenticationConfigResponse build() {
                return response;
            }
        }
    }
}