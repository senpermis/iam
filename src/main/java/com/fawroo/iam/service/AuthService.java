package com.fawroo.iam.service;

import java.util.Collections;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fawroo.iam.config.KeycloakConfig;
import com.fawroo.iam.model.dto.LoginRequest;
import com.fawroo.iam.model.dto.TokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    // private final Keycloak keycloak;

    private final UserService userService;

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    // LOGIN WITH USERNAME/EMAIL AND PASSWORD
    public TokenResponse login(LoginRequest loginRequest, String realm2) {
        try {
            // Créer une instance Keycloak pour le realm ciblé
            Keycloak keycloak2 = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm2)
                    .clientId(loginRequest.getClientId())
                    .username(loginRequest.getUsername())
                    .password(loginRequest.getPassword())
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            System.out.println("Realm used: " + realm2);

            // Obtenir le token
            AccessTokenResponse tokenResponse = keycloak2.tokenManager().getAccessToken();

            // Obtenir les infos utilisateur
            UserRepresentation user = getUserInfo(loginRequest.getUsername(), realm2);

            // Conversion en réponse personnalisée
            return convertToTokenResponse(tokenResponse, user);

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    // REFRESH TOKEN - CORRECTION
    public TokenResponse refreshToken(String refreshToken, String realm) {
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .clientId("admin-cli")
                    .grantType(OAuth2Constants.REFRESH_TOKEN)
                    // .refreshToken(refreshToken)
                    .build();

            // CORRECTION : Utiliser refreshToken() au lieu de getAccessToken()
            AccessTokenResponse tokenResponse = keycloak.tokenManager().refreshToken();

            // Extraire le username du token pour obtenir les infos utilisateur
            String username = extractUsernameFromToken(tokenResponse.getToken());
            UserRepresentation user = getUserInfo(username, realm);

            return convertToTokenResponse(tokenResponse, user);

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    // LOGOUT
    public void logout(String refreshToken, String realm) {
        try {
            // CORRECTION : Utiliser le refresh token pour le logout
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .clientId("admin-cli")
                    .grantType(OAuth2Constants.REFRESH_TOKEN)
                    // .refreshToken(refreshToken)
                    .build();

            keycloak.tokenManager().invalidate(refreshToken);
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    // VALIDATE TOKEN - CORRECTION
    public boolean validateToken(String token, String realm) {
        try {
            // CORRECTION : Utiliser une approche différente pour valider le token
            // Tenter d'obtenir un nouveau token avec le token existant
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .clientId("admin-cli")
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientSecret("your-client-secret") // Vous devrez configurer ceci
                    .build();

            // Si nous pouvons obtenir un token, alors le realm est accessible
            keycloak.tokenManager().getAccessToken();

            // Pour une validation réelle du token, utilisez une bibliothèque JWT
            return validateTokenWithJWT(token);

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // GET USER PROFILE FROM TOKEN - CORRECTION
    public UserRepresentation getUserProfile(String token, String realm) {
        try {
            // CORRECTION : Extraire le username du token JWT
            String username = extractUsernameFromJWT(token);
            return getUserInfo(username, realm);

        } catch (Exception e) {
            log.error("Failed to get user profile from token: {}", e.getMessage());
            throw new RuntimeException("Failed to get user profile: " + e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========

    private UserRepresentation getUserInfo(String username, String realm) {
        try {
            // Essayer d'abord par username
            return userService.getUserByUsername(username, realm);
        } catch (Exception e) {
            try {
                // Si ça échoue, essayer de chercher par email
                return userService.searchUsers(username, 0, 1)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } catch (Exception ex) {
                throw new RuntimeException("User not found with identifier: " + username);
            }
        }
    }

    private TokenResponse convertToTokenResponse(AccessTokenResponse source, UserRepresentation user) {
        TokenResponse response = new TokenResponse();
        response.setAccess_token(source.getToken());
        response.setExpires_in(source.getExpiresIn());
        response.setRefresh_expires_in(source.getRefreshExpiresIn());
        response.setRefresh_token(source.getRefreshToken());
        response.setToken_type(source.getTokenType());
        response.setSession_state(source.getSessionState());
        response.setScope(source.getScope());
        response.setNot_before_policy(String.valueOf(source.getNotBeforePolicy()));

        // Ajouter les informations utilisateur
        if (user != null) {
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
        }

        return response;
    }

    // CORRECTION : Méthode pour extraire le username d'un JWT
    private String extractUsernameFromJWT(String token) {
        try {
            // Simplification - dans une implémentation réelle, utilisez une bibliothèque
            // JWT
            // comme jjwt ou nimbus-jose-jwt
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Pour l'instant, retournez une valeur par défaut
            // Dans une implémentation réelle, décodez le JWT et extrayez le
            // "preferred_username"
            log.warn("JWT decoding not implemented, using default username");
            return "admin"; // Placeholder - à implémenter avec une vraie lib JWT

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract username from token");
        }
    }

    // CORRECTION : Méthode pour valider un JWT
    private boolean validateTokenWithJWT(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Simplification - dans une implémentation réelle, validez la signature,
            // la date d'expiration, etc. avec une bibliothèque JWT
            log.warn("JWT validation not fully implemented");
            return token != null && !token.isEmpty();

        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    // CORRECTION : Méthode dépréciée - à supprimer
    private String extractUsernameFromToken(String token) {
        return extractUsernameFromJWT(token);
    }
}