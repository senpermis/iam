package com.fawroo.iam.controller;

import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;
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

import com.fawroo.iam.service.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/realms/{realmName}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Void> createRealmRole(@PathVariable String realmName, 
                                              @RequestBody RoleRepresentation roleRepresentation) {
        roleService.createRealmRole(realmName, roleRepresentation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping
    public ResponseEntity<List<RoleRepresentation>> getAllRealmRoles(@PathVariable String realmName) {
        List<RoleRepresentation> roles = roleService.getAllRealmRoles(realmName);
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/{roleName}")
    public ResponseEntity<RoleRepresentation> getRealmRole(@PathVariable String realmName, 
                                                         @PathVariable String roleName) {
        RoleRepresentation role = roleService.getRealmRole(realmName, roleName);
        return ResponseEntity.ok(role);
    }
    
    @PutMapping("/{roleName}")
    public ResponseEntity<Void> updateRealmRole(@PathVariable String realmName, 
                                              @PathVariable String roleName,
                                              @RequestBody RoleRepresentation roleRepresentation) {
        roleService.updateRealmRole(realmName, roleName, roleRepresentation);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteRealmRole(@PathVariable String realmName, 
                                              @PathVariable String roleName) {
        roleService.deleteRealmRole(realmName, roleName);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/clients/{clientId}")
    public ResponseEntity<Void> createClientRole(@PathVariable String realmName,
                                               @PathVariable String clientId,
                                               @RequestBody RoleRepresentation roleRepresentation) {
        roleService.createClientRole(realmName, clientId, roleRepresentation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/clients/{clientId}")
    public ResponseEntity<List<RoleRepresentation>> getAllClientRoles(@PathVariable String realmName,
                                                                    @PathVariable String clientId) {
        List<RoleRepresentation> roles = roleService.getAllClientRoles(realmName, clientId);
        return ResponseEntity.ok(roles);
    }
}