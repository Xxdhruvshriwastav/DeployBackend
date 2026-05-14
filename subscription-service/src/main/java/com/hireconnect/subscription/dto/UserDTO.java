package com.hireconnect.subscription.dto;

import lombok.Data;

@Data
public class UserDTO {
    private int userId;
    private String email;
    private String role;
    private String password;
    private String provider;
    private boolean isActive;
}
