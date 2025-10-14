package com.fawroo.iam.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final Keycloak keycloak;

    // CREATE AUTHENTICATION FLOW
    public void createAuthenticationFlow(String realmName, AuthenticationFlowRepresentation flow) {
        try {
            keycloak.realms().realm(realmName).flows().createFlow(flow);
            log.info("Authentication flow created successfully: {} in realm {}", flow.getAlias(), realmName);
        } catch (Exception e) {
            log.error("Error creating authentication flow: {}", e.getMessage());
            throw new RuntimeException("Failed to create authentication flow: " + e.getMessage());
        }
    }

    // GET ALL AUTHENTICATION FLOWS
    public List<AuthenticationFlowRepresentation> getAuthenticationFlows(String realmName) {
        try {
            return keycloak.realms().realm(realmName).flows().getFlows();
        } catch (Exception e) {
            log.error("Error getting authentication flows for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get authentication flows: " + e.getMessage());
        }
    }

    // ADD EXECUTION TO FLOW
    public void addExecutionToFlow(String realmName, String flowAlias,
            AuthenticationExecutionRepresentation execution) {
        try {
            var flowsResource = keycloak.realms().realm(realmName).flows();

            // Créer la représentation pour l'ajout avec les bonnes propriétés
            Map<String, String> executionData = new HashMap<>();
            executionData.put("provider", execution.getAuthenticator());
            executionData.put("type", execution.getRequirement());

            flowsResource.addExecution(flowAlias, executionData);
            log.info("Execution added to flow: {} in realm {}", flowAlias, realmName);
        } catch (Exception e) {
            log.error("Error adding execution to flow {}: {}", flowAlias, e.getMessage());
            throw new RuntimeException("Failed to add execution to flow: " + e.getMessage());
        }
    }

    // GET REQUIRED ACTIONS
    public List<RequiredActionProviderRepresentation> getRequiredActions(String realmName) {
        try {
            return keycloak.realms().realm(realmName).flows().getRequiredActions();
        } catch (Exception e) {
            log.error("Error getting required actions for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get required actions: " + e.getMessage());
        }
    }

    // UPDATE REQUIRED ACTION
    public void updateRequiredAction(String realmName, String alias, RequiredActionProviderRepresentation action) {
        try {
            keycloak.realms().realm(realmName).flows().updateRequiredAction(alias, action);
            log.info("Required action updated successfully: {} in realm {}", alias, realmName);
        } catch (Exception e) {
            log.error("Error updating required action {}: {}", alias, e.getMessage());
            throw new RuntimeException("Failed to update required action: " + e.getMessage());
        }
    }
}