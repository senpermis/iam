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
import com.fawroo.iam.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody UserRequest userRequest,  @RequestParam(required = true) String realm) {
        String userId = userService.createUser(userRequest, realm);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("userId", userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUser(@PathVariable String userId, @RequestParam(required = true) String realm) {
        UserProfile profile = userService.getUserProfile(userId, realm);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfile> getUserByUsername(@PathVariable String username, @RequestParam(required = true) String realm) {
        UserRepresentation user = userService.getUserByUsername(username, realm);
        UserProfile profile = mapToUserProfile(user);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers(@RequestParam(required = true) String realm) {
        List<UserRepresentation> users = userService.getAllUsers(realm);
        List<UserProfile> profiles = users.stream()
                .map(this::mapToUserProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfile>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "20") int max,
            @RequestParam(required = true) String realm) {
        List<UserRepresentation> users = userService.searchUsers(query, first, max, realm);
        List<UserProfile> profiles = users.stream()
                .map(this::mapToUserProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable String userId,
            @RequestBody UserRequest userRequest,
            @RequestParam(required = true) String realm) {
        userService.updateUser(userId, userRequest, realm);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId, @RequestParam(required = true) String realm) {
        userService.deleteUser(userId, realm);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable String userId,
            @RequestBody PasswordUpdateRequest passwordRequest,
            @RequestParam(required = true) String realm) {
        userService.updatePassword(userId, passwordRequest, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/sessions")
    public ResponseEntity<List<UserSessionRepresentation>> getUserSessions(@PathVariable String userId, @RequestParam(required = true) String realm) {
        List<UserSessionRepresentation> sessions = userService.getUserSessions(userId, realm);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/{userId}/logout")
    public ResponseEntity<Void> logoutUser(@PathVariable String userId, @RequestParam(required = true) String realm) {
        userService.logoutUser(userId, realm);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> setUserStatus(@PathVariable String userId,
            @RequestParam boolean enabled,
            @RequestParam(required = true) String realm) {
        userService.setUserEnabled(userId, enabled, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getUsersCount(@RequestParam(required = true) String realm) {
        Integer count = userService.getUsersCount(realm);
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }

    @GetMapping("/{userId}/groups")
    public ResponseEntity<List<GroupRepresentation>> getUserGroups(@PathVariable String userId, @RequestParam(required = true) String realm) {
        List<GroupRepresentation> groups = userService.getUserGroups(userId, realm);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<Void> addUserToGroup(@PathVariable String userId,
            @PathVariable String groupId, @RequestParam(required = true) String realm) {
        userService.addUserToGroup(userId, groupId, realm);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<Void> removeUserFromGroup(@PathVariable String userId,
            @PathVariable String groupId, @RequestParam(required = true) String realm) {
        userService.removeUserFromGroup(userId, groupId, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<List<RoleRepresentation>> getUserRoles(@PathVariable String userId, @RequestParam(required = true) String realm) {
        List<RoleRepresentation> roles = userService.getUserRoles(userId, realm);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable String userId,
            @RequestBody RoleRepresentation role,
            @RequestParam(required = true) String realm) {
        userService.assignRoleToUser(userId, role, realm);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String userId,
            @RequestBody RoleRepresentation role,
            @RequestParam(required = true) String realm) {
        userService.removeRoleFromUser(userId, role, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/clients/{clientId}/roles")
    public ResponseEntity<List<RoleRepresentation>> getUserClientRoles(
            @PathVariable String userId,
            @PathVariable String clientId,
            @RequestParam(required = true) String realm) {
        List<RoleRepresentation> roles = userService.getUserClientRoles(userId, clientId, realm);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/{userId}/send-verification-email")
    public ResponseEntity<Void> sendVerificationEmail(@PathVariable String userId, @RequestParam(required = true) String realm) {
        userService.sendVerificationEmail(userId, realm);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/send-reset-password")
    public ResponseEntity<Void> sendResetPassword(@PathVariable String userId, @RequestParam(required = true) String realm) {
        userService.sendResetPassword(userId, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/consents")
    public ResponseEntity<List<Map<String, Object>>> getUserConsents(@PathVariable String userId, @RequestParam(required = true) String realm) {
        List<Map<String, Object>> consents = userService.getUserConsents(userId, realm);
        return ResponseEntity.ok(consents);
    }

    @DeleteMapping("/{userId}/consents")
    public ResponseEntity<Void> revokeUserConsents(@PathVariable String userId, @RequestParam(required = true) String realm) {
        userService.revokeUserConsents(userId, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/offline-sessions/{clientId}")
    public ResponseEntity<List<UserSessionRepresentation>> getOfflineSessions(
            @PathVariable String userId,
            @PathVariable String clientId,
            @RequestParam(required = true) String realm) {
        List<UserSessionRepresentation> sessions = userService.getOfflineSessions(userId, clientId, realm);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{userId}/federated-identity")
    public ResponseEntity<List<FederatedIdentityRepresentation>> getUserFederatedIdentity(
            @PathVariable String userId, @RequestParam(required = true) String realm) {
        List<FederatedIdentityRepresentation> federatedIdentity = userService.getUserFederatedIdentity(userId, realm);
        return ResponseEntity.ok(federatedIdentity);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable String userId, @RequestParam(required = true) String realm) {
        boolean exists = userService.userExists(userId, realm);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @PostMapping("/bulk/disable")
    public ResponseEntity<Void> bulkDisableUsers(@RequestBody List<String> userIds, @RequestParam(required = true) String realm) {
        userService.bulkDisableUsers(userIds, realm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestParam(required = true) String realm) {
        Integer totalUsers = userService.getUsersCount(realm);
        List<UserRepresentation> enabledUsers = userService.searchUsers("enabled:true", 0, 1000, realm);

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