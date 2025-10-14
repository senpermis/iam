package com.fawroo.iam.service;

import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final Keycloak keycloak;

    // GET REALM EVENTS
    public List<EventRepresentation> getRealmEvents(String realmName, Map<String, String> params) {
        try {
            return keycloak.realms().realm(realmName).getEvents(
                    params.get("type") != null ? List.of(params.get("type").split(",")) : null,
                    params.get("client"),
                    params.get("user"),
                    params.get("dateFrom"),
                    params.get("dateTo"),
                    params.get("ipAddress"),
                    Integer.parseInt(params.getOrDefault("first", "0")),
                    Integer.parseInt(params.getOrDefault("max", "100")));
        } catch (Exception e) {
            log.error("Error getting realm events for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get realm events: " + e.getMessage());
        }
    }

    // GET ADMIN EVENTS
    public List<AdminEventRepresentation> getAdminEvents(String realmName, Map<String, String> params) {
        try {
            return keycloak.realms().realm(realmName).getAdminEvents(
                    params.get("operationType") != null ? List.of(params.get("operationType").split(",")) : null,
                    params.get("authRealm"),
                    params.get("authClient"),
                    params.get("authUser"),
                    params.get("authIpAddress"),
                    params.get("resourcePath"),
                    params.get("resourceType") != null ? List.of(params.get("resourceType").split(",")) : null,
                    params.get("dateFrom"),
                    params.get("dateTo"),
                    params.get("first") != null ? Integer.parseInt(params.get("first")) : null,
                    params.get("max") != null ? Integer.parseInt(params.get("max")) : null);
        } catch (Exception e) {
            log.error("Error getting admin events for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get admin events: " + e.getMessage());
        }
    }

    // GET EVENT CONFIG
    public RealmEventsConfigRepresentation getEventConfig(String realmName) {
        try {
            return keycloak.realms().realm(realmName).getRealmEventsConfig();
        } catch (Exception e) {
            log.error("Error getting event config for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to get event config: " + e.getMessage());
        }
    }

    // UPDATE EVENT CONFIG
    public void updateEventConfig(String realmName, RealmEventsConfigRepresentation config) {
        try {
            keycloak.realms().realm(realmName).updateRealmEventsConfig(config);
            log.info("Event config updated successfully for realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error updating event config for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to update event config: " + e.getMessage());
        }
    }

    // CLEAR EVENTS
    public void clearEvents(String realmName) {
        try {
            keycloak.realms().realm(realmName).clearEvents();
            log.info("Events cleared successfully for realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error clearing events for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to clear events: " + e.getMessage());
        }
    }

    // CLEAR ADMIN EVENTS
    public void clearAdminEvents(String realmName) {
        try {
            keycloak.realms().realm(realmName).clearAdminEvents();
            log.info("Admin events cleared successfully for realm: {}", realmName);
        } catch (Exception e) {
            log.error("Error clearing admin events for {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Failed to clear admin events: " + e.getMessage());
        }
    }
}