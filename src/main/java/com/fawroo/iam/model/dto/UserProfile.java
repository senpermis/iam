package com.fawroo.iam.model.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private Map<String, List<String>> attributes;
}