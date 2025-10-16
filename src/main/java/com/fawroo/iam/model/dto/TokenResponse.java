package com.fawroo.iam.model.dto;

import lombok.Data;


@Data
public class TokenResponse {
    private String access_token;
    private long expires_in;
    private long refresh_expires_in;
    private String refresh_token;
    private String token_type;
    private String session_state;
    private String scope;
    private String not_before_policy;
    
    // Informations utilisateur supplémentaires
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}