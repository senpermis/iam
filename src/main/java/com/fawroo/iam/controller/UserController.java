package com.fawroo.iam.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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

    // UserController.java - Ajoutez ces endpoints

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getUsersCount() {
        Integer count = keycloakService.getUsersCount();
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }

    @GetMapping("/{userId}/groups")
    public ResponseEntity<List<GroupRepresentation>> getUserGroups(@PathVariable String userId) {
        List<GroupRepresentation> groups = keycloakService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<Void> addUserToGroup(@PathVariable String userId,
            @PathVariable String groupId) {
        keycloakService.addUserToGroup(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<Void> removeUserFromGroup(@PathVariable String userId,
            @PathVariable String groupId) {
        keycloakService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<List<RoleRepresentation>> getUserRoles(@PathVariable String userId) {
        List<RoleRepresentation> roles = keycloakService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable String userId,
            @RequestBody RoleRepresentation role) {
        keycloakService.assignRoleToUser(userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String userId,
            @RequestBody RoleRepresentation role) {
        keycloakService.removeRoleFromUser(userId, role);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/clients/{clientId}/roles")
    public ResponseEntity<List<RoleRepresentation>> getUserClientRoles(
            @PathVariable String userId,
            @PathVariable String clientId) {
        List<RoleRepresentation> roles = keycloakService.getUserClientRoles(userId, clientId);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/{userId}/send-verification-email")
    public ResponseEntity<Void> sendVerificationEmail(@PathVariable String userId) {
        keycloakService.sendVerificationEmail(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/send-reset-password")
    public ResponseEntity<Void> sendResetPassword(@PathVariable String userId) {
        keycloakService.sendResetPassword(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/consents")
    public ResponseEntity<List<Map<String, Object>>> getUserConsents(@PathVariable String userId) {
        List<Map<String, Object>> consents = keycloakService.getUserConsents(userId);
        return ResponseEntity.ok(consents);
    }

    @DeleteMapping("/{userId}/consents")
    public ResponseEntity<Void> revokeUserConsents(@PathVariable String userId) {
        keycloakService.revokeUserConsents(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/offline-sessions/{clientId}")
    public ResponseEntity<List<UserSessionRepresentation>> getOfflineSessions(
            @PathVariable String userId,
            @PathVariable String clientId) {
        List<UserSessionRepresentation> sessions = keycloakService.getOfflineSessions(userId, clientId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{userId}/federated-identity")
    public ResponseEntity<List<FederatedIdentityRepresentation>> getUserFederatedIdentity(
            @PathVariable String userId) {
        List<FederatedIdentityRepresentation> federatedIdentity = keycloakService.getUserFederatedIdentity(userId);
        return ResponseEntity.ok(federatedIdentity);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable String userId) {
        boolean exists = keycloakService.userExists(userId);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @PostMapping("/bulk/disable")
    public ResponseEntity<Void> bulkDisableUsers(@RequestBody List<String> userIds) {
        keycloakService.bulkDisableUsers(userIds);
        return ResponseEntity.ok().build();
    }

    // Endpoint pour récupérer les statistiques utilisateur
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Integer totalUsers = keycloakService.getUsersCount();
        List<UserRepresentation> enabledUsers = keycloakService.searchUsers("enabled:true", 0, 1000);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("enabledUsers", enabledUsers.size());
        stats.put("disabledUsers", totalUsers - enabledUsers.size());
        stats.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(stats);
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