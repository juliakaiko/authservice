package com.mymicroservice.authservice.integration.service;

import com.mymicroservice.authservice.configuration.AbstractContainerTest;
import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.repository.RefreshTokenRepository;
import com.mymicroservice.authservice.repository.UserCredentialRepository;
import com.mymicroservice.authservice.service.AuthService;
import com.mymicroservice.authservice.util.AuthRequestGenerator;
import com.mymicroservice.authservice.util.data.TestConstants;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class AuthFlowIT extends AbstractContainerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userCredentialRepository.deleteAll();
    }

    @Test
    void fullAuthFlow_ShouldCompleteSuccessfully_WhenCredentialsAreValid() {
        UserRegistrationRequest registrationRequest = AuthRequestGenerator.generateRegistrationRequest();
        AuthResponse registerResponse = authService.register(registrationRequest);

        assertNotNull(registerResponse.getAccessToken());
        assertNotNull(registerResponse.getRefreshToken());
        assertTrue(authService.validateToken(registerResponse.getAccessToken()));

        UserCredential savedUser = userCredentialRepository.findByEmailIgnoreCase(TestConstants.USER_EMAIL)
                .orElseThrow();
        assertTrue(passwordEncoder.matches(TestConstants.USER_PASSWORD, savedUser.getPassword()));
        assertTrue(refreshTokenRepository.findByUserEmailIgnoreCase(TestConstants.USER_EMAIL).isPresent());

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail(registrationRequest.getEmail());
        loginRequest.setPassword(registrationRequest.getPassword());
        AuthResponse loginResponse = authService.authenticate(loginRequest);

        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(loginResponse.getRefreshToken());
        AuthResponse refreshResponse = authService.refreshToken(refreshRequest);

        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());
        assertTrue(authService.validateToken(refreshResponse.getAccessToken()));
        assertFalse(authService.validateToken(TestConstants.INVALID_TOKEN));

        authService.deleteUserCredential(savedUser.getUserId());

        assertTrue(userCredentialRepository.findById(savedUser.getUserId()).isEmpty());
        assertTrue(refreshTokenRepository.findByUserEmailIgnoreCase(TestConstants.USER_EMAIL).isEmpty());
    }
}
