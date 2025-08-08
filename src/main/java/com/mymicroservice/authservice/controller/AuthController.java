package com.mymicroservice.authservice.controller;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Save user credentials and create tokens")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "LogIn the user", description = "Find the user and create tokens")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(summary = "Refresh the token", description = "Accepts and generate a new refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Validate JWT token", description = "Checks if the token is valid")
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(@Parameter(description = "JWT token (without 'Bearer')", required = true, example = "eyJhbGciOiJ...")
                                            @RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
