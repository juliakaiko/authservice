package com.mymicroservice.authservice.util;

import com.mymicroservice.authservice.model.RefreshToken;

import java.time.LocalDateTime;

public class RefreshTokenGenerator {

    public static RefreshToken generateRefreshToken() {

        return  RefreshToken.builder()
                .userEmail("test@test.by")
                .refreshToken("newRefreshToken")
                .expiresAt(LocalDateTime.of(2025,8,4, 22,17, 37))
                .issuedAt(LocalDateTime.of(2025,8,5, 22,17, 37))
                .build();
    }
}
