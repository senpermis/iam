package com.fawroo.iam.controller;

import java.util.Collections;
import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fawroo.iam.model.dto.LoginRequest;
import com.fawroo.iam.model.dto.TokenResponse;
import com.fawroo.iam.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // LOGIN ENDPOINT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, @RequestParam String realm) {
        try {
            TokenResponse tokenResponse = authService.login(loginRequest, realm);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // REFRESH TOKEN ENDPOINT
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request, @RequestParam String realm) {
        try {
            String refreshToken = request.get("refresh_token");
            TokenResponse tokenResponse = authService.refreshToken(refreshToken, realm);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid refresh token"));
        }
    }

    // LOGOUT ENDPOINT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request, @RequestParam String realm) {
        try {
            String userId = request.get("user_id");
            authService.logout(userId, realm);
            return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // VALIDATE TOKEN ENDPOINT
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String realm) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            boolean isValid = authService.validateToken(token, realm);
            
            if (isValid) {
                return ResponseEntity.ok(Collections.singletonMap("valid", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("valid", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("valid", false));
        }
    }

    // GET USER PROFILE FROM TOKEN
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String realm) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            UserRepresentation userProfile = authService.getUserProfile(token, realm);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid token"));
        }
    }


    // ========== HELPER METHODS ==========

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}