package com.mymicroservice.authservice.util;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.mapper.UserCredentialMapper;
import com.mymicroservice.authservice.model.UserCredential;

public class AuthRequestGenerator {

    public static AuthRequest generateAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setEmail(com.mymicroservice.authservice.util.data.TestConstants.LOGIN_EMAIL);
        request.setPassword(com.mymicroservice.authservice.util.data.TestConstants.LOGIN_PASSWORD);
        return request;
    }

    public static RefreshTokenRequest generateRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(com.mymicroservice.authservice.util.data.TestConstants.MOCK_REFRESH_TOKEN);
        return request;
    }

    public static UserRegistrationRequest generateRegistrationRequest() {
        UserCredential user = UserCredentialGenerator.generateUser();
        return UserCredentialMapper.INSTANSE.toDto(user);
    }
}
