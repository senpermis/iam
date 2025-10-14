package com.fawroo.iam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.*;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class BackupService {

    private final Keycloak keycloak;
    private final ObjectMapper objectMapper;

    // SAUVEGARDER UN REALM COMPLET
    public String backupRealm(String realmName, String backupDirectory) {
        try {
            // Créer le dossier de backup s'il n'existe pas
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // Générer un nom de fichier avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("backup_%s_%s.json", realmName, timestamp);
            String filePath = backupPath.resolve(fileName).toString();

            // Récupérer toute la configuration du realm
            Map<String, Object> realmConfig = getAllRealmConfiguration(realmName);

            // Sauvegarder dans un fichier JSON
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(filePath), realmConfig);

            log.info("Backup completed successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error backing up realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Backup failed: " + e.getMessage());
        }
    }

    // SAUVEGARDER TOUS LES REALMS
    public String backupAllRealms(String backupDirectory) {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("backup_all_realms_%s.json", timestamp);
            String filePath = backupPath.resolve(fileName).toString();

            Map<String, Object> allRealmsConfig = new HashMap<>();
            List<RealmRepresentation> realms = keycloak.realms().findAll();

            for (RealmRepresentation realm : realms) {
                allRealmsConfig.put(realm.getRealm(), getAllRealmConfiguration(realm.getRealm()));
            }

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(filePath), allRealmsConfig);

            log.info("Full backup completed successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error backing up all realms: {}", e.getMessage());
            throw new RuntimeException("Full backup failed: " + e.getMessage());
        }
    }

    // SAUVEGARDER EN FORMAT ZIP
    public String backupRealmToZip(String realmName, String backupDirectory) {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String zipFileName = String.format("backup_%s_%s.zip", realmName, timestamp);
            String zipFilePath = backupPath.resolve(zipFileName).toString();

            Map<String, Object> realmConfig = getAllRealmConfiguration(realmName);

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath)))) {
                // Fichier principal de configuration
                ZipEntry configEntry = new ZipEntry("realm-config.json");
                zos.putNextEntry(configEntry);
                zos.write(objectMapper.writeValueAsBytes(realmConfig));
                zos.closeEntry();

                // Fichier séparé pour les utilisateurs avec rôles
                ZipEntry usersEntry = new ZipEntry("users-with-roles.json");
                zos.putNextEntry(usersEntry);
                zos.write(objectMapper.writeValueAsBytes(realmConfig.get("usersWithRoles")));
                zos.closeEntry();
            }

            log.info("ZIP backup completed successfully: {}", zipFilePath);
            return zipFilePath;

        } catch (Exception e) {
            log.error("Error creating ZIP backup for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("ZIP backup failed: " + e.getMessage());
        }
    }

    // RÉCUPÉRER TOUTE LA CONFIGURATION D'UN REALM
    private Map<String, Object> getAllRealmConfiguration(String realmName) {
        Map<String, Object> config = new HashMap<>();

        try {
            var realmResource = keycloak.realms().realm(realmName);

            // Configuration de base du realm
            config.put("realm", realmResource.toRepresentation());

            // Clients
            config.put("clients", realmResource.clients().findAll());

            // Rôles du realm
            config.put("roles", realmResource.roles().list());

            // Utilisateurs AVEC LEURS RÔLES
            config.put("usersWithRoles", getUsersWithRoles(realmName));

            // Utilisateurs de base (sans rôles - pour compatibilité)
            config.put("users", realmResource.users().list(0, 1000));

            // Groupes
            config.put("groups", realmResource.groups().groups());

            // Identity Providers
            config.put("identityProviders", realmResource.identityProviders().findAll());

            // Client Scopes
            config.put("clientScopes", realmResource.clientScopes().findAll());

            // Configuration des événements
            config.put("eventsConfig", realmResource.getRealmEventsConfig());

            // Récupérer la configuration d'autorisation pour chaque client
            Map<String, Object> clientsAuthz = new HashMap<>();
            for (ClientRepresentation client : realmResource.clients().findAll()) {
                try {
                    var authzSettings = realmResource.clients().get(client.getId()).authorization();
                    ResourceServerRepresentation authzConfig = authzSettings.exportSettings();
                    clientsAuthz.put(client.getClientId(), authzConfig);
                } catch (Exception e) {
                    log.debug("No authorization settings for client: {}", client.getClientId());
                }
            }
            config.put("authorizationSettings", clientsAuthz);

            // Métadonnées de sauvegarde
            config.put("backupMetadata", Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "realmName", realmName,
                    "version", "1.0",
                    "usersCount", getUsersWithRoles(realmName).size()));

        } catch (Exception e) {
            log.error("Error fetching configuration for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Configuration fetch failed: " + e.getMessage());
        }

        return config;
    }

    // RÉCUPÉRER LES UTILISATEURS AVEC LEURS RÔLES (version corrigée)
private List<Map<String, Object>> getUsersWithRoles(String realmName) {
    List<Map<String, Object>> usersWithRoles = new ArrayList<>();

    try {
        var realmResource = keycloak.realms().realm(realmName);
        var users = realmResource.users().list(0, 1000);
        var clients = realmResource.clients().findAll();

        for (UserRepresentation user : users) {
            Map<String, Object> userWithRoles = new HashMap<>();

            // Informations de base
            userWithRoles.put("id", user.getId());
            userWithRoles.put("username", user.getUsername());
            userWithRoles.put("email", user.getEmail());
            userWithRoles.put("firstName", user.getFirstName());
            userWithRoles.put("lastName", user.getLastName());
            userWithRoles.put("enabled", user.isEnabled());
            userWithRoles.put("emailVerified", user.isEmailVerified());
            userWithRoles.put("createdTimestamp", user.getCreatedTimestamp());
            userWithRoles.put("attributes", user.getAttributes());

            try {
                var userResource = realmResource.users().get(user.getId());

                // Rôles du realm
                var realmRoles = userResource.roles().realmLevel().listAll();
                List<String> realmRoleNames = realmRoles.stream()
                        .map(RoleRepresentation::getName)
                        .collect(Collectors.toList());
                
                System.out.println("User: " + user.getUsername() + " - Realm Roles: " + realmRoleNames);
                userWithRoles.put("realmRoles", realmRoleNames);

                // CORRECTION : Rôles clients - approche manuelle avec chaque client
                Map<String, List<String>> clientRoles = new HashMap<>();
                
                for (ClientRepresentation client : clients) {
                    try {
                        // CORRECTION : clientLevel() nécessite l'UUID du client
                        var clientRolesForUser = userResource.roles().clientLevel(client.getId()).listAll();
                        if (clientRolesForUser != null && !clientRolesForUser.isEmpty()) {
                            List<String> roleNames = clientRolesForUser.stream()
                                    .map(RoleRepresentation::getName)
                                    .collect(Collectors.toList());
                            clientRoles.put(client.getClientId(), roleNames);
                            System.out.println("Client: " + client.getClientId() + " - Roles: " + roleNames);
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs pour ce client (peut ne pas avoir de rôles)
                        log.debug("No roles for user {} in client {}: {}", user.getUsername(), client.getClientId(), e.getMessage());
                    }
                }
                userWithRoles.put("clientRoles", clientRoles);

                // Groupes
                var groups = userResource.groups();
                List<String> groupNames = groups.stream()
                        .map(GroupRepresentation::getName)
                        .collect(Collectors.toList());
                userWithRoles.put("groups", groupNames);

            } catch (Exception e) {
                log.warn("Could not fetch detailed roles for user {}: {}", user.getUsername(), e.getMessage());
                userWithRoles.put("realmRoles", List.of());
                userWithRoles.put("clientRoles", Map.of());
                userWithRoles.put("groups", List.of());
            }

            usersWithRoles.add(userWithRoles);
        }

        log.info("Fetched {} users with roles for realm: {}", usersWithRoles.size(), realmName);

    } catch (Exception e) {
        log.error("Error fetching users with roles for realm {}: {}", realmName, e.getMessage());
        throw new RuntimeException("Failed to fetch users with roles: " + e.getMessage());
    }

    return usersWithRoles;
}


    // SAUVEGARDER UNIQUEMENT LES UTILISATEURS AVEC RÔLES
    public String backupUsersWithRoles(String realmName, String backupDirectory) {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("backup_%s_users_with_roles_%s.json", realmName, timestamp);
            String filePath = backupPath.resolve(fileName).toString();

            List<Map<String, Object>> usersWithRoles = getUsersWithRoles(realmName);

            Map<String, Object> backupData = new HashMap<>();
            backupData.put("users", usersWithRoles);
            backupData.put("backupMetadata", Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "realmName", realmName,
                    "totalUsers", usersWithRoles.size()));

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(filePath), backupData);

            log.info("Users with roles backup completed successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error backing up users with roles for realm {}: {}", realmName, e.getMessage());
            throw new RuntimeException("Users with roles backup failed: " + e.getMessage());
        }
    }

    // SAUVEGARDER UN ÉLÉMENT SPÉCIFIQUE
    public String backupSpecificElement(String realmName, String elementType, String backupDirectory) {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("backup_%s_%s_%s.json", realmName, elementType, timestamp);
            String filePath = backupPath.resolve(fileName).toString();

            Object elementData;

            if ("usersWithRoles".equalsIgnoreCase(elementType)) {
                elementData = getUsersWithRoles(realmName);
            } else {
                elementData = getSpecificElementData(realmName, elementType);
            }

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(filePath), elementData);

            log.info("{} backup completed successfully: {}", elementType, filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error backing up {} for realm {}: {}", elementType, realmName, e.getMessage());
            throw new RuntimeException(elementType + " backup failed: " + e.getMessage());
        }
    }

    private Object getSpecificElementData(String realmName, String elementType) {
        var realmResource = keycloak.realms().realm(realmName);

        return switch (elementType.toLowerCase()) {
            case "clients" -> realmResource.clients().findAll();
            case "users" -> realmResource.users().list(0, 5000);
            case "roles" -> realmResource.roles().list();
            case "groups" -> realmResource.groups().groups();
            case "identityproviders" -> realmResource.identityProviders().findAll();
            case "clientscopes" -> realmResource.clientScopes().findAll();
            case "userswithroles" -> getUsersWithRoles(realmName);
            default -> throw new IllegalArgumentException("Unsupported element type: " + elementType);
        };
    }
}