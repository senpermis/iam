package com.fawroo.iam.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fawroo.iam.model.dto.PasswordUpdateRequest;
import com.fawroo.iam.model.dto.UserProfile;
import com.fawroo.iam.model.dto.UserRequest;
import com.fawroo.iam.service.KeycloakService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final KeycloakService keycloakService;
    
    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody UserRequest userRequest) {
        String userId = keycloakService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("userId", userId));
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUser(@PathVariable String userId) {
        UserProfile profile = keycloakService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfile> getUserByUsername(@PathVariable String username) {
        UserRepresentation user = keycloakService.getUserByUsername(username);
        UserProfile profile = mapToUserProfile(user);
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        List<UserRepresentation> users = keycloakService.getAllUsers();
        List<UserProfile> profiles = users.stream()
                .map(this::mapToUserProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UserProfile>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "20") int max) {
        List<UserRepresentation> users = keycloakService.searchUsers(query, first, max);
        List<UserProfile> profiles = users.stream()
                .map(this::mapToUserProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable String userId, 
                                         @RequestBody UserRequest userRequest) {
        keycloakService.updateUser(userId, userRequest);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        keycloakService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable String userId,
                                             @RequestBody PasswordUpdateRequest passwordRequest) {
        keycloakService.updatePassword(userId, passwordRequest);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{userId}/sessions")
    public ResponseEntity<List<UserSessionRepresentation>> getUserSessions(@PathVariable String userId) {
        List<UserSessionRepresentation> sessions = keycloakService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
    @PostMapping("/{userId}/logout")
    public ResponseEntity<Void> logoutUser(@PathVariable String userId) {
        keycloakService.logoutUser(userId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> setUserStatus(@PathVariable String userId,
                                            @RequestParam boolean enabled) {
        keycloakService.setUserEnabled(userId, enabled);
        return ResponseEntity.ok().build();
    }
    
    private UserProfile mapToUserProfile(UserRepresentation user) {
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEnabled(user.isEnabled());
        profile.setAttributes(user.getAttributes());
        return profile;
    }
}