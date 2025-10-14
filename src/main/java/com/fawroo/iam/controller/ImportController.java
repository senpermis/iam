package com.fawroo.iam.controller;

import com.fawroo.iam.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping(value = "/realm", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> importRealm(@RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File is empty"));
            }

            String result = importService.importRealm(file);
            return ResponseEntity.ok(Collections.singletonMap("message", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/realm/with-options", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> importRealmWithOptions(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean importUsers,
            @RequestParam(defaultValue = "true") boolean importClients,
            @RequestParam(defaultValue = "true") boolean importRoles,
            @RequestParam(defaultValue = "true") boolean importGroups,
            @RequestParam(defaultValue = "false") boolean skipExisting) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File is empty"));
            }

            String result = importService.importRealmWithOptions(file, importUsers, importClients, importRoles,
                    importGroups, skipExisting);
            return ResponseEntity.ok(Collections.singletonMap("message", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/realm/config-only", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> importRealmConfigOnly(@RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File is empty"));
            }

            String result = importService.importRealmConfigOnly(file);
            return ResponseEntity.ok(Collections.singletonMap("message", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Alternative: Import via JSON body (sans fichier)
    @PostMapping(value = "/realm/json", consumes = "application/json")
    public ResponseEntity<Map<String, String>> importRealmFromJson(@RequestBody Map<String, Object> realmConfig) {
        try {
            String result = importService.importRealmFromJson(realmConfig);
            return ResponseEntity.ok(Collections.singletonMap("message", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}