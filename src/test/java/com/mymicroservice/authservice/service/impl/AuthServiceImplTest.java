package com.mymicroservice.authservice.service.impl;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.exception.InvalidCredentialsException;
import com.mymicroservice.authservice.mapper.UserCredentialMapper;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.repositiry.UserCredentialRepository;
import com.mymicroservice.authservice.service.JwtService;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserCredentialRepository userCredentialRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private UserCredential testUser;
    private UserRegistrationRequest registrationRequest;
    private AuthRequest authRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        testUser = UserCredentialGenerator.generateUser();
        testUser.setUserId(1l);
        registrationRequest = UserCredentialMapper.INSTANSE.toDto(testUser);

        authRequest = new AuthRequest();
        authRequest.setEmail("test@test.by");
        authRequest.setPassword("pass_test");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refreshToken");
    }

    // Registration Tests
    @Test
    void register_NewUser_ReturnsAuthResponse() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("pass_test");
        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyString(),anyList())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyString(),anyList())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registrationRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(userCredentialRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(userCredentialRepository).save(any(UserCredential.class));
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of("USER"));
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of("USER"));
        verify(jwtService).saveRefreshToken("refreshToken");
    }

    @Test
    void register_ExistingUser_ThrowsException() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.register(registrationRequest));
        verify(userCredentialRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(userCredentialRepository, never()).save(any());
    }

    @Test
    void register_AdminRole_SavesAsAdmin() {
        registrationRequest.setRole(Role.ADMIN);
        testUser.setRole(Role.ADMIN);

        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyString(),anyList())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyString(),anyList())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registrationRequest);

        assertNotNull(response);
        verify(userCredentialRepository).save(argThat(user -> user.getRole() == Role.ADMIN));
    }

    // Authenticate Tests
    @Test
    void authenticate_ValidCredentials_ReturnsAuthResponse() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(anyString(),anyList())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyString(),anyList())).thenReturn("refreshToken");

        AuthResponse response = authService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(userCredentialRepository).findByEmailIgnoreCase(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of("USER"));
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of("USER"));
        verify(jwtService).saveRefreshToken("refreshToken");
    }

    @Test
    void authenticate_InvalidEmail_ThrowsException() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(authRequest));
        verify(userCredentialRepository).findByEmailIgnoreCase(authRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(authRequest));
        verify(userCredentialRepository).findByEmailIgnoreCase(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPassword());
    }

    // Refresh Token Tests
    @Test
    void refreshToken_ValidToken_ReturnsNewAuthResponse() {
        when(jwtService.extractUsername(anyString())).thenReturn(testUser.getUsername());
        when(jwtService.generateAccessToken(anyString(),anyList())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(anyString(),anyList())).thenReturn("newRefreshToken");

        AuthResponse response = authService.refreshToken(refreshTokenRequest);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());

        verify(jwtService).extractUsername(refreshTokenRequest.getRefreshToken());
        verify(jwtService).deleteRefreshTokenByUserEmail(testUser.getUsername());
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of());
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of());
        verify(jwtService).saveRefreshToken("newRefreshToken");
    }

    // Validate Token Tests
    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        when(jwtService.isTokenValid(anyString())).thenReturn(true);

        boolean isValid = authService.validateToken("validToken");

        assertTrue(isValid);
        verify(jwtService).isTokenValid("validToken");
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        boolean isValid = authService.validateToken("invalidToken");

        assertFalse(isValid);
        verify(jwtService).isTokenValid("invalidToken");
    }
}
