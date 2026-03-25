package com.ecommerce.backend.dto;

import lombok.Data;
import com.ecommerce.backend.enums.Role;

@Data
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private Role role;
}