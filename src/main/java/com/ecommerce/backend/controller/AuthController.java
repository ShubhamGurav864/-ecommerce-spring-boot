package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ApiResponse;
import com.ecommerce.backend.dto.LoginRequest;
import com.ecommerce.backend.dto.RegisterRequest;
import com.ecommerce.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return new ApiResponse("User Registered", "success");
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest request) {
        String token = userService.loginUser(request);
        return new ApiResponse(token, "success");
    }
}