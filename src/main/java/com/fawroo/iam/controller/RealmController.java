package com.fawroo.iam.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fawroo.iam.service.KeycloakService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/realms")
@RequiredArgsConstructor
public class RealmController {

    private final KeycloakService keycloakService;

    @PostMapping
    public ResponseEntity<Void> createRealm(@RequestBody RealmRepresentation realmRepresentation) {
        keycloakService.createRealm(realmRepresentation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping
    public ResponseEntity<List<RealmRepresentation>> getAllRealms() {
        List<RealmRepresentation> realms = keycloakService.getAllRealms();
        return ResponseEntity.ok(realms);
    }
    
    @GetMapping("/{realmName}")
    public ResponseEntity<RealmRepresentation> getRealm(@PathVariable String realmName) {
        RealmRepresentation realm = keycloakService.getRealm(realmName);
        return ResponseEntity.ok(realm);
    }
    
    @PutMapping("/{realmName}")
    public ResponseEntity<Void> updateRealm(@PathVariable String realmName, 
                                          @RequestBody RealmRepresentation realmRepresentation) {
        keycloakService.updateRealm(realmName, realmRepresentation);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{realmName}")
    public ResponseEntity<Void> deleteRealm(@PathVariable String realmName) {
        keycloakService.deleteRealm(realmName);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{realmName}/exists")
    public ResponseEntity<Map<String, Boolean>> realmExists(@PathVariable String realmName) {
        boolean exists = keycloakService.realmExists(realmName);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }
    
    @GetMapping("/{realmName}/stats")
    public ResponseEntity<Map<String, Object>> getRealmStats(@PathVariable String realmName) {
        Map<String, Object> stats = keycloakService.getRealmStatistics(realmName);
        return ResponseEntity.ok(stats);
    }
}