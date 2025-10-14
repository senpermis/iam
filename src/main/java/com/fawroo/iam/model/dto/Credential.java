package com.fawroo.iam.model.dto;

import lombok.Data;

@Data
public class Credential {
    private String type = "password";
    private String value;
    private boolean temporary = false;
}