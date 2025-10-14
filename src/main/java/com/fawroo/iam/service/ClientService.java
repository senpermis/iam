package com.fawroo.iam.service;

import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientService {

    private final Keycloak keycloak;

    // CREATE CLIENT
    public String createClient(String realmName, ClientRepresentation clientRepresentation) {
        try {
            var response = keycloak.realms().realm(realmName).clients().create(clientRepresentation);
            log.info("Client created successfully: {} in realm {}", clientRepresentation.getClientId(), realmName);
            return "Client created successfully";
        } catch (Exception e) {
            log.error("Error creating client: {}", e.getMessage());
            throw new RuntimeException("Failed to create client: " + e.getMessage());
        }
    }

    // GET ALL CLIENTS
    public List<ClientRepresentation> getAllClients(String realmName) {
        try {
            return keycloak.realms().realm(realmName).clients().findAll();
        } catch (Exception e) {
            log.error("Error getting clients for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get clients: " + e.getMessage());
        }
    }

    // GET CLIENT BY ID
    public ClientRepresentation getClientById(String realmName, String clientId) {
        try {
            return keycloak.realms().realm(realmName).clients().get(clientId).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Client not found: " + e.getMessage());
        }
    }

    // GET CLIENT BY CLIENT_ID
    public ClientRepresentation getClientByClientId(String realmName, String clientId) {
        try {
            List<ClientRepresentation> clients = keycloak.realms().realm(realmName).clients().findByClientId(clientId);
            if (clients.isEmpty()) {
                throw new RuntimeException("Client not found with clientId: " + clientId);
            }
            return clients.get(0);
        } catch (Exception e) {
            log.error("Error getting client by clientId {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to get client: " + e.getMessage());
        }
    }

    // UPDATE CLIENT
    public void updateClient(String realmName, String clientId, ClientRepresentation clientRepresentation) {
        try {
            keycloak.realms().realm(realmName).clients().get(clientId).update(clientRepresentation);
            log.info("Client updated successfully: {} in realm {}", clientId, realmName);
        } catch (Exception e) {
            log.error("Error updating client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to update client: " + e.getMessage());
        }
    }

    // DELETE CLIENT
    public void deleteClient(String realmName, String clientId) {
        try {
            keycloak.realms().realm(realmName).clients().get(clientId).remove();
            log.info("Client deleted successfully: {} from realm {}", clientId, realmName);
        } catch (Exception e) {
            log.error("Error deleting client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to delete client: " + e.getMessage());
        }
    }

    // GENERATE CLIENT SECRET
    public Map<String, String> generateClientSecret(String realmName, String clientId) {
        try {
            var credential = keycloak.realms().realm(realmName).clients().get(clientId).getSecret();
            return Map.of("secret", credential.getValue());
        } catch (Exception e) {
            log.error("Error generating client secret for {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to generate client secret: " + e.getMessage());
        }
    }

    // GET CLIENT SCOPES
    public List<ClientScopeRepresentation> getClientScopes(String realmName, String clientId) {
        try {
            return keycloak.realms().realm(realmName).clients().get(clientId).getDefaultClientScopes();
        } catch (Exception e) {
            log.error("Error getting client scopes for {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to get client scopes: " + e.getMessage());
        }
    }

    // ADD PROTOCOL MAPPER TO CLIENT
    public void addProtocolMapper(String realmName, String clientId, ProtocolMapperRepresentation mapper) {
        try {
            keycloak.realms().realm(realmName).clients().get(clientId).getProtocolMappers().createMapper(mapper);
            log.info("Protocol mapper added to client: {}", clientId);
        } catch (Exception e) {
            log.error("Error adding protocol mapper to client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to add protocol mapper: " + e.getMessage());
        }
    }
}