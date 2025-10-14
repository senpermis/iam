package com.fawroo.iam.controller;

import com.fawroo.iam.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @GetMapping("/realms/{realmName}")
    public ResponseEntity<Map<String, String>> backupRealm(
            @PathVariable String realmName,
            @RequestParam(defaultValue = "./backups") String backupDir) {
        String filePath = backupService.backupRealm(realmName, backupDir);
        return ResponseEntity.ok(Collections.singletonMap("backupPath", filePath));
    }

    @PostMapping("/realms/{realmName}/zip")
    public ResponseEntity<Map<String, String>> backupRealmToZip(
            @PathVariable String realmName,
            @RequestParam(defaultValue = "./backups") String backupDir) {
        String filePath = backupService.backupRealmToZip(realmName, backupDir);
        return ResponseEntity.ok(Collections.singletonMap("backupPath", filePath));
    }

    @PostMapping("/all-realms")
    public ResponseEntity<Map<String, String>> backupAllRealms(
            @RequestParam(defaultValue = "./backups") String backupDir) {
        String filePath = backupService.backupAllRealms(backupDir);
        return ResponseEntity.ok(Collections.singletonMap("backupPath", filePath));
    }

    @PostMapping("/realms/{realmName}/{elementType}")
    public ResponseEntity<Map<String, String>> backupSpecificElement(
            @PathVariable String realmName,
            @PathVariable String elementType,
            @RequestParam(defaultValue = "./backups") String backupDir) {
        String filePath = backupService.backupSpecificElement(realmName, elementType, backupDir);
        return ResponseEntity.ok(Collections.singletonMap("backupPath", filePath));
    }

    // NOUVEAU ENDPOINT PUT SAUVEGARDER LES UTILISATEURS AVEC RÔLES
    @PostMapping("/realms/{realmName}/users-with-roles")
    public ResponseEntity<Map<String, String>> backupUsersWithRoles(
            @PathVariable String realmName,
            @RequestParam(defaultValue = "./backups") String backupDir) {
        String filePath = backupService.backupUsersWithRoles(realmName, backupDir);
        return ResponseEntity.ok(Collections.singletonMap("backupPath", filePath));
    }
}