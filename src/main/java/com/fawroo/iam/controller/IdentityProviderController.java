package com.fawroo.iam.controller;

import com.fawroo.iam.service.IdentityProviderService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/realms/{realmName}/identity-providers")
@RequiredArgsConstructor
public class IdentityProviderController {

    private final IdentityProviderService identityProviderService;

    @PostMapping
    public ResponseEntity<String> createIdentityProvider(
            @PathVariable String realmName,
            @RequestBody IdentityProviderRepresentation provider) {
        identityProviderService.createIdentityProvider(realmName, provider);
        return ResponseEntity.ok("Identity provider created successfully");
    }

    @GetMapping
    public ResponseEntity<List<IdentityProviderRepresentation>> getAllIdentityProviders(
            @PathVariable String realmName) {
        List<IdentityProviderRepresentation> providers = identityProviderService.getAllIdentityProviders(realmName);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/{alias}")
    public ResponseEntity<IdentityProviderRepresentation> getIdentityProvider(
            @PathVariable String realmName,
            @PathVariable String alias) {
        IdentityProviderRepresentation provider = identityProviderService.getIdentityProvider(realmName, alias);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{alias}")
    public ResponseEntity<String> updateIdentityProvider(
            @PathVariable String realmName,
            @PathVariable String alias,
            @RequestBody IdentityProviderRepresentation provider) {
        identityProviderService.updateIdentityProvider(realmName, alias, provider);
        return ResponseEntity.ok("Identity provider updated successfully");
    }

    @DeleteMapping("/{alias}")
    public ResponseEntity<String> deleteIdentityProvider(
            @PathVariable String realmName,
            @PathVariable String alias) {
        identityProviderService.deleteIdentityProvider(realmName, alias);
        return ResponseEntity.ok("Identity provider deleted successfully");
    }
}