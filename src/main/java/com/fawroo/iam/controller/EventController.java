package com.fawroo.iam.controller;

import com.fawroo.iam.service.EventService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/realms/{realmName}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventRepresentation>> getRealmEvents(
            @PathVariable String realmName,
            @RequestParam Map<String, String> params) {
        List<EventRepresentation> events = eventService.getRealmEvents(realmName, params);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<AdminEventRepresentation>> getAdminEvents(
            @PathVariable String realmName,
            @RequestParam Map<String, String> params) {
        List<AdminEventRepresentation> events = eventService.getAdminEvents(realmName, params);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/config")
    public ResponseEntity<RealmEventsConfigRepresentation> getEventConfig(@PathVariable String realmName) {
        RealmEventsConfigRepresentation config = eventService.getEventConfig(realmName);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    public ResponseEntity<String> updateEventConfig(
            @PathVariable String realmName,
            @RequestBody RealmEventsConfigRepresentation config) {
        eventService.updateEventConfig(realmName, config);
        return ResponseEntity.ok("Event config updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> clearEvents(@PathVariable String realmName) {
        eventService.clearEvents(realmName);
        return ResponseEntity.ok("Events cleared successfully");
    }

    @DeleteMapping("/admin")
    public ResponseEntity<String> clearAdminEvents(@PathVariable String realmName) {
        eventService.clearAdminEvents(realmName);
        return ResponseEntity.ok("Admin events cleared successfully");
    }
}