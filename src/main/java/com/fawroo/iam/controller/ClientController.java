package com.fawroo.iam.controller;

import com.fawroo.iam.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/realms/{realmName}/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<String> createClient(
            @PathVariable String realmName,
            @RequestBody ClientRepresentation clientRepresentation) {
        String result = clientService.createClient(realmName, clientRepresentation);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ClientRepresentation>> getAllClients(@PathVariable String realmName) {
        List<ClientRepresentation> clients = clientService.getAllClients(realmName);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientRepresentation> getClientById(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        ClientRepresentation client = clientService.getClientById(realmName, clientId);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/by-client-id/{clientId}")
    public ResponseEntity<ClientRepresentation> getClientByClientId(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        ClientRepresentation client = clientService.getClientByClientId(realmName, clientId);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<String> updateClient(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ClientRepresentation clientRepresentation) {
        clientService.updateClient(realmName, clientId, clientRepresentation);
        return ResponseEntity.ok("Client updated successfully");
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> deleteClient(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        clientService.deleteClient(realmName, clientId);
        return ResponseEntity.ok("Client deleted successfully");
    }

    @PostMapping("/{clientId}/secret")
    public ResponseEntity<Map<String, String>> generateClientSecret(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        Map<String, String> secret = clientService.generateClientSecret(realmName, clientId);
        return ResponseEntity.ok(secret);
    }

    @GetMapping("/{clientId}/scopes")
    public ResponseEntity<List<ClientScopeRepresentation>> getClientScopes(
            @PathVariable String realmName,
            @PathVariable String clientId) {
        List<ClientScopeRepresentation> scopes = clientService.getClientScopes(realmName, clientId);
        return ResponseEntity.ok(scopes);
    }

    @PostMapping("/{clientId}/protocol-mappers")
    public ResponseEntity<String> addProtocolMapper(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ProtocolMapperRepresentation mapper) {
        clientService.addProtocolMapper(realmName, clientId, mapper);
        return ResponseEntity.ok("Protocol mapper added successfully");
    }
}