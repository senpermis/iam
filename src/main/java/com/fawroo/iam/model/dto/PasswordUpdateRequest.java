package com.fawroo.iam.model.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String newPassword;
    private boolean temporary = false;
}