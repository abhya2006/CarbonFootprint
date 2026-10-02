package com.ecotrack.controller;

import com.ecotrack.dto.ApiDtos.*;
import com.ecotrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register") public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }
    @GetMapping("/me") public UserResponse me(Authentication authentication) { return authService.me(authentication.getName()); }
    @PutMapping("/me") public AuthResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) { return authService.updateProfile(authentication.getName(), request); }
}