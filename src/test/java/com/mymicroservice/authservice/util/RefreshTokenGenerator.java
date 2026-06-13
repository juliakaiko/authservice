package com.mymicroservice.authservice.util;

import com.mymicroservice.authservice.model.RefreshToken;

import static com.mymicroservice.authservice.util.data.TestConstants.REFRESH_TOKEN_EXPIRES_AT;
import static com.mymicroservice.authservice.util.data.TestConstants.REFRESH_TOKEN_ISSUED_AT;
import static com.mymicroservice.authservice.util.data.TestConstants.REFRESH_TOKEN_VALUE;
import static com.mymicroservice.authservice.util.data.TestConstants.USER_EMAIL;

public class RefreshTokenGenerator {

    public static RefreshToken generateRefreshToken() {
        return RefreshToken.builder()
                .userEmail(USER_EMAIL)
                .refreshToken(REFRESH_TOKEN_VALUE)
                .expiresAt(REFRESH_TOKEN_EXPIRES_AT)
                .issuedAt(REFRESH_TOKEN_ISSUED_AT)
                .build();
    }
}
