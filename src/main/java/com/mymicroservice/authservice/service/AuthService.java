package com.mymicroservice.authservice.service;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;

public interface AuthService {

    AuthResponse register(UserRegistrationRequest request);
    AuthResponse authenticate(AuthRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    boolean validateToken(String token);
    void deleteUserCredential(Long userId);

}
