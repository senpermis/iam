package com.fawroo.iam.model.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // Peut être username ou email
    private String password;
    private String clientId = "admin-cli"; // Client par défaut
    private String grantType = "password";
}