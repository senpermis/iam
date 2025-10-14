package com.fawroo.iam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private final Keycloak keycloak;
    private final ObjectMapper objectMapper;

    // IMPORTER UN REALM COMPLET (version finale)
    public String importRealm(MultipartFile file) {
        try {
            // Lire le fichier JSON
            Map<String, Object> backupData = objectMapper.readValue(file.getInputStream(), Map.class);

            // Extraire la configuration du realm
            Map<String, Object> realmMap = (Map<String, Object>) backupData.get("realm");
            RealmRepresentation realmRep = objectMapper.convertValue(realmMap, RealmRepresentation.class);

            String realmName = realmRep.getRealm();

            // Vérifier si le realm existe déjà
            if (realmExists(realmName)) {
                throw new RuntimeException("Realm already exists: " + realmName);
            }

            // Nettoyer les champs problématiques avant création
            cleanRealmForImport(realmRep);

            // Créer le realm
            keycloak.realms().create(realmRep);
            log.info("Base realm created: {}", realmName);

            // Importer TOUS les éléments supplémentaires APRÈS la création du realm
            importAdditionalElements(realmName, backupData);

            log.info("Realm imported successfully with all elements: {}", realmName);
            return "Realm imported successfully with all roles, users, clients, etc: " + realmName;

        } catch (Exception e) {
            log.error("Error importing realm: {}", e.getMessage());
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    // IMPORTER TOUS LES ÉLÉMENTS SUPPLÉMENTAIRES (avec bon ordre)
    private void importAdditionalElements(String realmName, Map<String, Object> backupData) {
        try {
            log.info("Starting import of additional elements for realm: {}", realmName);

            // 1. Importer les client scopes d'abord (nécessaires pour les clients)
            importClientScopesToRealm(realmName, backupData);

            // 2. Importer les clients (nécessaires pour les rôles clients)
            importClientsToRealm(realmName, backupData);

            // 3. Importer les rôles du realm (y compris composites)
            importAllRolesToRealm(realmName, backupData); // Utilisez cette méthode pour tous les rôles

            // 4. Importer les identity providers
            importIdentityProvidersToRealm(realmName, backupData);

            // 5. Importer les groupes
            importGroupsToRealm(realmName, backupData);

            // 6. Importer les utilisateurs (en dernier car ils dépendent des rôles et
            // groupes)
            importUsersToRealm(realmName, backupData);

            // 7. Assigner les rôles aux utilisateurs
            assignRolesToUsers(realmName, backupData);

            log.info("Completed import of all additional elements for realm: {}", realmName);

        } catch (Exception e) {
            log.warn("Error importing some elements to realm {}: {}", realmName, e.getMessage());
        }
    }

    // ASSIGNER LES RÔLES AUX UTILISATEURS (version corrigée)
    private void assignRolesToUsers(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> usersWithRoles = (List<Map<String, Object>>) backupData.get("usersWithRoles");
            if (usersWithRoles != null) {
                int assignedCount = 0;

                for (Map<String, Object> userWithRoles : usersWithRoles) {
                    try {
                        String username = (String) userWithRoles.get("username");
                        if (username != null) {
                            // Trouver l'utilisateur créé
                            var users = keycloak.realms().realm(realmName).users().searchByUsername(username, true);
                            if (!users.isEmpty()) {
                                String userId = users.get(0).getId();
                                assignUserRoles(realmName, userId, userWithRoles);
                                assignedCount++;
                                log.info("Successfully assigned roles to user: {}", username);
                            } else {
                                log.warn("User not found after import: {}", username);
                            }
                        }
                    } catch (Exception userError) {
                        log.warn("Could not assign roles to user {}: {}", userWithRoles.get("username"),
                                userError.getMessage());
                    }
                }
                log.info("Assigned roles to {} users in realm: {}", assignedCount, realmName);
            } else {
                log.warn("No usersWithRoles found in backup data, cannot assign roles");
            }
        } catch (Exception e) {
            log.warn("Could not assign roles to users in realm {}: {}", realmName, e.getMessage());
        }
    }

    // IMPORTER TOUS LES RÔLES SANS FILTRE
    private void importAllRolesToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> rolesMap = (List<Map<String, Object>>) backupData.get("roles");
            if (rolesMap != null) {
                int importedCount = 0;

                for (Map<String, Object> roleMap : rolesMap) {
                    try {
                        RoleRepresentation role = objectMapper.convertValue(roleMap, RoleRepresentation.class);

                        // Nettoyer le rôle pour l'import
                        role.setId(null);

                        // Essayer de créer le rôle
                        try {
                            keycloak.realms().realm(realmName).roles().create(role);
                            importedCount++;
                            log.debug("Successfully imported role: {}", role.getName());

                            // Gérer les composites APRÈS la création
                            if (roleMap.containsKey("composites")) {
                                importRoleComposites(realmName, role.getName(), roleMap);
                            }

                        } catch (Exception e) {
                            // Si le rôle existe déjà, on continue
                            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                                log.debug("Role already exists, skipping: {}", role.getName());
                                continue;
                            }
                            throw e;
                        }

                    } catch (Exception roleError) {
                        log.warn("Could not import role {}: {}", roleMap.get("name"), roleError.getMessage());
                    }
                }
                log.info("Imported {} roles to realm: {}", importedCount, realmName);
            }
        } catch (Exception e) {
            log.error("Could not import roles to realm {}: {}", realmName, e.getMessage());
        }
    }

    /////////////////////////////////////////////
    ///
    ///
    ///
    ///
    ///
    ///
    ///

    // CORRECTION : Changer userMap en userWithRoles dans les signatures
    private void assignUserRoles(String realmName, String userId, Map<String, Object> userWithRoles) {
        try {
            var userResource = keycloak.realms().realm(realmName).users().get(userId);

            // Rôles du realm
            assignRealmRoles(userResource, realmName, userWithRoles);

            // Rôles clients
            assignClientRoles(userResource, realmName, userWithRoles);

        } catch (Exception e) {
            log.warn("Could not assign roles to user {}: {}", userId, e.getMessage());
        }
    }

    // CORRECTION : Changer userMap en userWithRoles
    private void assignRealmRoles(org.keycloak.admin.client.resource.UserResource userResource,
            String realmName, Map<String, Object> userWithRoles) {
        try {
            List<String> realmRoles = (List<String>) userWithRoles.get("realmRoles");
            if (realmRoles != null && !realmRoles.isEmpty()) {
                List<RoleRepresentation> rolesToAdd = new ArrayList<>();
                List<String> rolesNotFound = new ArrayList<>();
                List<String> defaultRolesSkipped = new ArrayList<>();

                String username = (String) userWithRoles.get("username");
                log.info("Processing {} realm roles for user: {}", realmRoles.size(), username);

                for (String roleName : realmRoles) {
                    try {
                        // Éviter les rôles par défaut qui sont assignés automatiquement
                        if (!isDefaultRole(roleName)) {
                            var role = keycloak.realms().realm(realmName).roles().get(roleName).toRepresentation();
                            rolesToAdd.add(role);
                            log.debug("✓ Found realm role: {}", roleName);
                        } else {
                            defaultRolesSkipped.add(roleName);
                            log.debug("↳ Skipping default realm role: {}", roleName);
                        }
                    } catch (Exception e) {
                        rolesNotFound.add(roleName);
                        log.warn("✗ Realm role not found: {}", roleName);
                    }
                }

                if (!rolesToAdd.isEmpty()) {
                    userResource.roles().realmLevel().add(rolesToAdd);
                    log.info("✅ Successfully assigned {} realm roles to user {}: {}",
                            rolesToAdd.size(), username,
                            rolesToAdd.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
                }

                if (!defaultRolesSkipped.isEmpty()) {
                    log.info("ℹ️ Skipped {} default roles for user {}: {}",
                            defaultRolesSkipped.size(), username, defaultRolesSkipped);
                }

                if (!rolesNotFound.isEmpty()) {
                    log.warn("⚠️ The following realm roles were not found for user {}: {}",
                            username, rolesNotFound);
                }
            } else {
                log.debug("No realm roles to assign");
            }
        } catch (Exception e) {
            log.warn("Error assigning realm roles: {}", e.getMessage());
        }
    }

    // CORRECTION : Changer userMap en userWithRoles
    private void assignClientRoles(org.keycloak.admin.client.resource.UserResource userResource,
            String realmName, Map<String, Object> userWithRoles) {
        try {
            Map<String, List<String>> clientRoles = (Map<String, List<String>>) userWithRoles.get("clientRoles");
            if (clientRoles != null && !clientRoles.isEmpty()) {
                String username = (String) userWithRoles.get("username");
                log.info("Processing client roles for user: {}", username);

                for (Map.Entry<String, List<String>> entry : clientRoles.entrySet()) {
                    String clientId = entry.getKey();
                    List<String> roles = entry.getValue();

                    if (roles != null && !roles.isEmpty()) {
                        // Trouver le client par son clientId
                        var clients = keycloak.realms().realm(realmName).clients().findByClientId(clientId);

                        if (!clients.isEmpty()) {
                            String clientUuid = clients.get(0).getId();
                            assignRolesToClient(userResource, realmName, clientUuid, clientId, roles, username);
                        } else {
                            log.warn("Client not found for roles assignment: {} for user {}", clientId, username);
                        }
                    }
                }
            } else {
                log.debug("No client roles to assign");
            }
        } catch (Exception e) {
            log.warn("Error assigning client roles: {}", e.getMessage());
        }
    }

    // CORRECTION : Ajouter le paramètre username pour le logging
    private void assignRolesToClient(org.keycloak.admin.client.resource.UserResource userResource,
            String realmName, String clientUuid, String clientId,
            List<String> roleNames, String username) {
        try {
            List<RoleRepresentation> rolesToAdd = new ArrayList<>();
            List<String> rolesNotFound = new ArrayList<>();

            for (String roleName : roleNames) {
                try {
                    var role = keycloak.realms().realm(realmName).clients()
                            .get(clientUuid).roles().get(roleName).toRepresentation();
                    rolesToAdd.add(role);
                    log.debug("✓ Found client role: {} for client {}", roleName, clientId);
                } catch (Exception e) {
                    rolesNotFound.add(roleName);
                    log.warn("✗ Client role not found: {} for client {}", roleName, clientId);
                }
            }

            if (!rolesToAdd.isEmpty()) {
                userResource.roles().clientLevel(clientUuid).add(rolesToAdd);
                log.info("✅ Successfully assigned {} client roles to user {} for client {}: {}",
                        rolesToAdd.size(), username, clientId,
                        rolesToAdd.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            }

            if (!rolesNotFound.isEmpty()) {
                log.warn("⚠️ The following client roles were not found for user {} in client {}: {}",
                        username, clientId, rolesNotFound);
            }
        } catch (Exception e) {
            log.warn("Error assigning roles to client {} for user {}: {}", clientId, username, e.getMessage());
        }
    }

    ///
    ///
    /// 
    /// 
    // VÉRIFIER LES RÔLES PAR DÉFAUT (version améliorée)
private boolean isDefaultRole(String roleName) {
    if (roleName == null) return true;

    // Rôles par défaut de Keycloak qu'il ne faut pas assigner manuellement
    return roleName.startsWith("default-roles-") || 
           "offline_access".equals(roleName) || 
           "uma_authorization".equals(roleName) ||
           roleName.contains("-default") ||
           roleName.equals("user") ||
           roleName.equals("admin");
}

    // ASSIGNER LES RÔLES POUR UN CLIENT SPÉCIFIQUE (version améliorée)
    private void assignRolesToClient(org.keycloak.admin.client.resource.UserResource userResource,
            String realmName, String clientUuid, String clientId, List<String> roleNames) {
        try {
            List<RoleRepresentation> rolesToAdd = new ArrayList<>();
            List<String> rolesNotFound = new ArrayList<>();

            for (String roleName : roleNames) {
                try {
                    var role = keycloak.realms().realm(realmName).clients()
                            .get(clientUuid).roles().get(roleName).toRepresentation();
                    rolesToAdd.add(role);
                    log.debug("Found client role: {} for client {}", roleName, clientId);
                } catch (Exception e) {
                    rolesNotFound.add(roleName);
                    log.debug("Client role not found: {} for client {}", roleName, clientId);
                }
            }

            if (!rolesToAdd.isEmpty()) {
                userResource.roles().clientLevel(clientUuid).add(rolesToAdd);
                log.info("Successfully assigned {} client roles to user for client {}: {}",
                        rolesToAdd.size(), clientId,
                        rolesToAdd.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            }

            if (!rolesNotFound.isEmpty()) {
                log.warn("The following client roles were not found for client {}: {}", clientId, rolesNotFound);
            }
        } catch (Exception e) {
            log.warn("Error assigning roles to client {}: {}", clientId, e.getMessage());
        }
    }

    // ASSIGNER LES RÔLES POUR UN CLIENT SPÉCIFIQUE
    private void assignRolesToClient(org.keycloak.admin.client.resource.UserResource userResource,
            String realmName, String clientUuid, List<String> roleNames) {
        try {
            List<RoleRepresentation> rolesToAdd = new ArrayList<>();

            for (String roleName : roleNames) {
                try {
                    var role = keycloak.realms().realm(realmName).clients()
                            .get(clientUuid).roles().get(roleName).toRepresentation();
                    rolesToAdd.add(role);
                } catch (Exception e) {
                    log.debug("Client role not found, skipping: {} for client {}", roleName, clientUuid);
                }
            }

            if (!rolesToAdd.isEmpty()) {
                userResource.roles().clientLevel(clientUuid).add(rolesToAdd);
                log.debug("Assigned {} client roles to user for client {}", rolesToAdd.size(), clientUuid);
            }
        } catch (Exception e) {
            log.warn("Error assigning roles to client {}: {}", clientUuid, e.getMessage());
        }
    }

    // IMPORTER LES RÔLES (version complète)
    private void importRolesToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> rolesMap = (List<Map<String, Object>>) backupData.get("roles");
            if (rolesMap != null) {
                int importedCount = 0;
                int skippedCount = 0;

                for (Map<String, Object> roleMap : rolesMap) {
                    try {
                        RoleRepresentation role = objectMapper.convertValue(roleMap, RoleRepresentation.class);

                        // Nettoyer le rôle pour l'import
                        role.setId(null);

                        // Vérifier si c'est un rôle par défaut à éviter
                        if (isDefaultRole(role.getName())) {
                            skippedCount++;
                            continue;
                        }

                        // Créer le rôle
                        keycloak.realms().realm(realmName).roles().create(role);
                        importedCount++;

                        // Gérer les rôles composites APRÈS la création
                        if (roleMap.containsKey("composites")) {
                            importRoleComposites(realmName, role.getName(), roleMap);
                        }

                    } catch (Exception roleError) {
                        log.warn("Could not import role {}: {}", roleMap.get("name"), roleError.getMessage());
                    }
                }
                log.info("Imported {} roles, skipped {} default roles to realm: {}", importedCount, skippedCount,
                        realmName);
            }
        } catch (Exception e) {
            log.error("Could not import roles to realm {}: {}", realmName, e.getMessage());
        }
    }

    // IMPORTER LES COMPOSITES DES RÔLES
    private void importRoleComposites(String realmName, String roleName, Map<String, Object> roleMap) {
        try {
            Map<String, Object> compositesMap = (Map<String, Object>) roleMap.get("composites");
            if (compositesMap != null && !compositesMap.isEmpty()) {
                var roleResource = keycloak.realms().realm(realmName).roles().get(roleName);

                // Gérer les composites realm
                if (compositesMap.containsKey("realm")) {
                    List<Map<String, Object>> realmComposites = (List<Map<String, Object>>) compositesMap.get("realm");
                    for (Map<String, Object> compositeMap : realmComposites) {
                        String compositeRoleName = (String) compositeMap.get("name");
                        if (compositeRoleName != null) {
                            try {
                                // Récupérer le rôle composite existant
                                var compositeRole = keycloak.realms().realm(realmName).roles().get(compositeRoleName)
                                        .toRepresentation();
                                roleResource.addComposites(List.of(compositeRole));
                            } catch (Exception e) {
                                log.warn("Could not add composite realm role {} to role {}: {}", compositeRoleName,
                                        roleName, e.getMessage());
                            }
                        }
                    }
                }

                // Gérer les composites clients
                if (compositesMap.containsKey("client")) {
                    Map<String, List<Map<String, Object>>> clientComposites = (Map<String, List<Map<String, Object>>>) compositesMap
                            .get("client");
                    for (Map.Entry<String, List<Map<String, Object>>> entry : clientComposites.entrySet()) {
                        String clientId = entry.getKey();
                        List<Map<String, Object>> clientRoles = entry.getValue();

                        for (Map<String, Object> clientRoleMap : clientRoles) {
                            String clientRoleName = (String) clientRoleMap.get("name");
                            if (clientRoleName != null) {
                                try {
                                    // Récupérer le rôle client existant
                                    var clientRole = keycloak.realms().realm(realmName).clients().get(clientId).roles()
                                            .get(clientRoleName).toRepresentation();
                                    roleResource.addComposites(List.of(clientRole));
                                } catch (Exception e) {
                                    log.warn("Could not add composite client role {} to role {}: {}", clientRoleName,
                                            roleName, e.getMessage());
                                }
                            }
                        }
                    }
                }

                log.debug("Imported composites for role: {}", roleName);
            }
        } catch (Exception e) {
            log.warn("Could not import composites for role {}: {}", roleName, e.getMessage());
        }
    }



    // IMPORTER LES GROUPES
    private void importGroupsToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> groupsMap = (List<Map<String, Object>>) backupData.get("groups");
            if (groupsMap != null) {
                for (Map<String, Object> groupMap : groupsMap) {
                    GroupRepresentation group = objectMapper.convertValue(groupMap, GroupRepresentation.class);
                    // Nettoyer le groupe pour l'import
                    group.setId(null);

                    keycloak.realms().realm(realmName).groups().add(group);
                }
                log.info("Imported {} groups to realm: {}", groupsMap.size(), realmName);
            }
        } catch (Exception e) {
            log.warn("Could not import groups to realm {}: {}", realmName, e.getMessage());
        }
    }

    // IMPORTER LES IDENTITY PROVIDERS
    private void importIdentityProvidersToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> idpsMap = (List<Map<String, Object>>) backupData.get("identityProviders");
            if (idpsMap != null) {
                for (Map<String, Object> idpMap : idpsMap) {
                    IdentityProviderRepresentation idp = objectMapper.convertValue(idpMap,
                            IdentityProviderRepresentation.class);
                    // Nettoyer l'identity provider pour l'import
                    idp.setInternalId(null);

                    keycloak.realms().realm(realmName).identityProviders().create(idp);
                }
                log.info("Imported {} identity providers to realm: {}", idpsMap.size(), realmName);
            }
        } catch (Exception e) {
            log.warn("Could not import identity providers to realm {}: {}", realmName, e.getMessage());
        }
    }

    // IMPORTER LES CLIENT SCOPES
    private void importClientScopesToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> scopesMap = (List<Map<String, Object>>) backupData.get("clientScopes");
            if (scopesMap != null) {
                for (Map<String, Object> scopeMap : scopesMap) {
                    ClientScopeRepresentation scope = objectMapper.convertValue(scopeMap,
                            ClientScopeRepresentation.class);
                    // Nettoyer le client scope pour l'import
                    scope.setId(null);

                    keycloak.realms().realm(realmName).clientScopes().create(scope);
                }
                log.info("Imported {} client scopes to realm: {}", scopesMap.size(), realmName);
            }
        } catch (Exception e) {
            log.warn("Could not import client scopes to realm {}: {}", realmName, e.getMessage());
        }
    }

    // IMPORTER LES UTILISATEURS (version corrigée)
    private void importUsersToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> usersMap = (List<Map<String, Object>>) backupData.get("users");
            if (usersMap != null) {
                int importedCount = 0;
                for (Map<String, Object> userMap : usersMap) {
                    try {
                        UserRepresentation user = objectMapper.convertValue(userMap, UserRepresentation.class);
                        // Nettoyer l'utilisateur pour l'import
                        user.setId(null);
                        user.setCreatedTimestamp(null);
                        user.setFederationLink(null);

                        keycloak.realms().realm(realmName).users().create(user);
                        importedCount++;

                    } catch (Exception userError) {
                        log.warn("Could not import user {}: {}", userMap.get("username"), userError.getMessage());
                    }
                }
                log.info("Imported {} users to realm: {}", importedCount, realmName);
            }
        } catch (Exception e) {
            log.warn("Could not import users to realm {}: {}", realmName, e.getMessage());
        }
    }

    // IMPORTER LES CLIENTS (version corrigée)
    private void importClientsToRealm(String realmName, Map<String, Object> backupData) {
        try {
            List<Map<String, Object>> clientsMap = (List<Map<String, Object>>) backupData.get("clients");
            if (clientsMap != null) {
                int importedCount = 0;
                for (Map<String, Object> clientMap : clientsMap) {
                    try {
                        ClientRepresentation client = objectMapper.convertValue(clientMap, ClientRepresentation.class);
                        // Nettoyer le client pour l'import
                        client.setId(null);

                        // Éviter les clients sensibles
                        if (!isSensitiveClient(client.getClientId())) {
                            keycloak.realms().realm(realmName).clients().create(client);
                            importedCount++;
                        }

                    } catch (Exception clientError) {
                        log.warn("Could not import client {}: {}", clientMap.get("clientId"), clientError.getMessage());
                    }
                }
                log.info("Imported {} clients to realm: {}", importedCount, realmName);
            }
        } catch (Exception e) {
            log.warn("Could not import clients to realm {}: {}", realmName, e.getMessage());
        }
    }

    // NETTOYER LE REALM POUR L'IMPORT (version corrigée)
    private void cleanRealmForImport(RealmRepresentation realm) {
        // Réinitialiser l'ID (doit être généré par Keycloak)
        realm.setId(null);

        // Réinitialiser les compteurs
        realm.setNotBefore(0);

        // NE PAS supprimer les listes d'éléments - elles seront importées séparément
        // Garder les références mais les créer après le realm

        // S'assurer que le realm est enabled
        realm.setEnabled(true);

        // Éviter les conflits avec le realm master
        if (realm.getRealm() != null && realm.getRealm().equalsIgnoreCase("master")) {
            throw new RuntimeException("Cannot import master realm");
        }
    }

    // VÉRIFIER LES CLIENTS SENSIBLES
    private boolean isSensitiveClient(String clientId) {
        return clientId == null ||
                "master-realm".equals(clientId) ||
                "security-admin-console".equals(clientId) ||
                "admin-cli".equals(clientId) ||
                "broker".equals(clientId);
    }

    // VÉRIFIER SI LE REALM EXISTE
    private boolean realmExists(String realmName) {
        try {
            keycloak.realms().realm(realmName).toRepresentation();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // IMPORTER AVEC OPTIONS (version corrigée)
    public String importRealmWithOptions(MultipartFile file, boolean importUsers, boolean importClients,
            boolean importRoles, boolean importGroups, boolean skipExisting) {
        try {
            Map<String, Object> backupData = objectMapper.readValue(file.getInputStream(), Map.class);
            Map<String, Object> realmMap = (Map<String, Object>) backupData.get("realm");
            RealmRepresentation realmRep = objectMapper.convertValue(realmMap, RealmRepresentation.class);

            String realmName = realmRep.getRealm();

            // Vérifier si le realm existe déjà
            if (skipExisting && realmExists(realmName)) {
                return "Realm already exists, skipping: " + realmName;
            }

            cleanRealmForImport(realmRep);

            // Créer le realm de base d'abord
            keycloak.realms().create(realmRep);
            log.info("Base realm created: {}", realmName);

            // Importer les éléments supplémentaires selon les options
            if (importRoles) {
                importRolesToRealm(realmName, backupData);
            }

            if (importGroups) {
                importGroupsToRealm(realmName, backupData);
            }

            if (importClients) {
                importClientsToRealm(realmName, backupData);
            }

            if (importUsers) {
                importUsersToRealm(realmName, backupData);
            }

            return "Realm imported successfully with selected options: " + realmName;

        } catch (Exception e) {
            log.error("Error importing realm with options: {}", e.getMessage());
            throw new RuntimeException("Import with options failed: " + e.getMessage());
        }
    }

    // IMPORTER DEPUIS UN FICHIER LOCAL (version corrigée)
    public String importRealmFromFile(String filePath) {
        try {
            File file = new File(filePath);
            Map<String, Object> backupData = objectMapper.readValue(file, Map.class);

            Map<String, Object> realmMap = (Map<String, Object>) backupData.get("realm");
            RealmRepresentation realmRep = objectMapper.convertValue(realmMap, RealmRepresentation.class);

            cleanRealmForImport(realmRep);
            keycloak.realms().create(realmRep);

            String realmName = realmRep.getRealm();

            // Importer les éléments supplémentaires
            importAdditionalElements(realmName, backupData);

            log.info("Realm imported successfully from file: {}", realmName);
            return "Realm imported successfully: " + realmName;

        } catch (Exception e) {
            log.error("Error importing realm from file: {}", e.getMessage());
            throw new RuntimeException("Import from file failed: " + e.getMessage());
        }
    }

    // IMPORTER DEPUIS UN JSON DIRECT
    public String importRealmFromJson(Map<String, Object> realmConfig) {
        try {
            RealmRepresentation realmRep = objectMapper.convertValue(realmConfig, RealmRepresentation.class);
            cleanRealmForImport(realmRep);
            keycloak.realms().create(realmRep);

            log.info("Realm imported successfully from JSON: {}", realmRep.getRealm());
            return "Realm imported successfully: " + realmRep.getRealm();

        } catch (Exception e) {
            log.error("Error importing realm from JSON: {}", e.getMessage());
            throw new RuntimeException("JSON import failed: " + e.getMessage());
        }
    }

    // IMPORTER UNIQUEMENT LA CONFIGURATION (sans les données)
    public String importRealmConfigOnly(MultipartFile file) {
        try {
            Map<String, Object> backupData = objectMapper.readValue(file.getInputStream(), Map.class);
            Map<String, Object> realmMap = (Map<String, Object>) backupData.get("realm");
            RealmRepresentation realmRep = objectMapper.convertValue(realmMap, RealmRepresentation.class);

            // Nettoyer radicalement pour une configuration seule
            cleanRealmForImport(realmRep);
            realmRep.setUsers(null);
            realmRep.setClients(null);
            realmRep.setRoles(null);
            realmRep.setGroups(null);
            realmRep.setDefaultGroups(null);

            keycloak.realms().create(realmRep);

            log.info("Realm configuration imported successfully: {}", realmRep.getRealm());
            return "Realm configuration imported successfully: " + realmRep.getRealm();

        } catch (Exception e) {
            log.error("Error importing realm configuration: {}", e.getMessage());
            throw new RuntimeException("Configuration import failed: " + e.getMessage());
        }
    }
}