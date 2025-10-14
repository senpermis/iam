package com.fawroo.iam.controller;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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

import com.fawroo.iam.service.GroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/realms/{realmName}/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createGroup(@PathVariable String realmName,
                                                         @RequestBody GroupRepresentation groupRepresentation) {
        String groupId = groupService.createGroup(realmName, groupRepresentation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("groupId", groupId));
    }
    
    @GetMapping
    public ResponseEntity<List<GroupRepresentation>> getAllGroups(@PathVariable String realmName) {
        List<GroupRepresentation> groups = groupService.getAllGroups(realmName);
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupRepresentation> getGroup(@PathVariable String realmName,
                                                      @PathVariable String groupId) {
        GroupRepresentation group = groupService.getGroup(realmName, groupId);
        return ResponseEntity.ok(group);
    }
    
    @GetMapping("/path/{path}")
    public ResponseEntity<GroupRepresentation> getGroupByPath(@PathVariable String realmName,
                                                            @PathVariable String path) {
        GroupRepresentation group = groupService.getGroupByPath(realmName, path);
        return ResponseEntity.ok(group);
    }
    
    @PutMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(@PathVariable String realmName,
                                          @PathVariable String groupId,
                                          @RequestBody GroupRepresentation groupRepresentation) {
        groupService.updateGroup(realmName, groupId, groupRepresentation);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String realmName,
                                          @PathVariable String groupId) {
        groupService.deleteGroup(realmName, groupId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserRepresentation>> getGroupMembers(@PathVariable String realmName,
                                                                  @PathVariable String groupId) {
        List<UserRepresentation> members = groupService.getGroupMembers(realmName, groupId);
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/{groupId}/roles")
    public ResponseEntity<List<RoleRepresentation>> getGroupRoles(@PathVariable String realmName,
                                                                @PathVariable String groupId) {
        List<RoleRepresentation> roles = groupService.getGroupRealmRoles(realmName, groupId);
        return ResponseEntity.ok(roles);
    }
    
    @PostMapping("/{groupId}/roles")
    public ResponseEntity<Void> assignRoleToGroup(@PathVariable String realmName,
                                                @PathVariable String groupId,
                                                @RequestBody RoleRepresentation role) {
        groupService.assignRoleToGroup(realmName, groupId, role);
        return ResponseEntity.ok().build();
    }
}