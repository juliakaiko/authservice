package com.mymicroservice.authservice.unit.service;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.exception.InvalidCredentialsException;
import com.mymicroservice.authservice.exception.UserCredentialNotFoundException;
import com.mymicroservice.authservice.mapper.UserCredentialMapper;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.repository.UserCredentialRepository;
import com.mymicroservice.authservice.service.JwtService;
import com.mymicroservice.authservice.service.impl.AuthServiceImpl;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import com.mymicroservice.authservice.util.data.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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
        testUser = UserCredentialGenerator.generateUserWithId();
        registrationRequest = UserCredentialMapper.INSTANSE.toDto(testUser);

        authRequest = new AuthRequest();
        authRequest.setEmail(TestConstants.USER_EMAIL);
        authRequest.setPassword(TestConstants.USER_PASSWORD);

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(TestConstants.MOCK_REFRESH_TOKEN);
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenNewUser() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(TestConstants.USER_PASSWORD);
        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_REFRESH_TOKEN);

        AuthResponse response = authService.register(registrationRequest);

        assertNotNull(response);
        assertEquals(TestConstants.MOCK_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestConstants.MOCK_REFRESH_TOKEN, response.getRefreshToken());

        verify(userCredentialRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(userCredentialRepository).save(any(UserCredential.class));
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).saveRefreshToken(TestConstants.MOCK_REFRESH_TOKEN);
    }

    @Test
    void register_ShouldThrowException_WhenUserExists() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.register(registrationRequest));
        verify(userCredentialRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(userCredentialRepository, never()).save(any());
    }

    @Test
    void register_ShouldSaveAsAdmin_WhenAdminRole() {
        registrationRequest.setRole(Role.ADMIN);
        testUser.setRole(Role.ADMIN);

        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(TestConstants.ENCODED_PASSWORD);
        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_REFRESH_TOKEN);

        AuthResponse response = authService.register(registrationRequest);

        assertNotNull(response);
        verify(userCredentialRepository).save(argThat(user -> user.getRole() == Role.ADMIN));
    }

    @Test
    void register_ShouldDefaultToUser_WhenRoleIsNull() {
        registrationRequest.setRole(null);

        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(TestConstants.ENCODED_PASSWORD);
        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_REFRESH_TOKEN);

        AuthResponse response = authService.register(registrationRequest);

        assertNotNull(response);
        verify(userCredentialRepository).save(argThat(user -> user.getRole() == Role.USER));
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
    }

    @Test
    void register_ShouldThrowException_WhenUnknownRole() {
        Role unknownRole = mock(Role.class);
        when(unknownRole.getAuthority()).thenReturn("MANAGER");
        registrationRequest.setRole(unknownRole);

        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.register(registrationRequest));
        verify(userCredentialRepository, never()).save(any());
    }

    @Test
    void authenticate_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(anyString(), anyList())).thenReturn(TestConstants.MOCK_REFRESH_TOKEN);

        AuthResponse response = authService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals(TestConstants.MOCK_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestConstants.MOCK_REFRESH_TOKEN, response.getRefreshToken());

        verify(userCredentialRepository).findByEmailIgnoreCase(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).saveRefreshToken(TestConstants.MOCK_REFRESH_TOKEN);
    }

    @Test
    void authenticate_ShouldThrowException_WhenEmailInvalid() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(authRequest));
        verify(userCredentialRepository).findByEmailIgnoreCase(authRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordInvalid() {
        when(userCredentialRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(authRequest));
        verify(userCredentialRepository).findByEmailIgnoreCase(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPassword());
    }

    @Test
    void refreshToken_ShouldReturnNewAuthResponse_WhenTokenIsValid() {
        when(jwtService.extractUsername(anyString())).thenReturn(testUser.getUsername());
        when(jwtService.getRoles(anyString())).thenReturn(List.of(TestConstants.ROLE_USER));
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn(TestConstants.NEW_ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(anyString(), anyList())).thenReturn(TestConstants.NEW_REFRESH_TOKEN);

        AuthResponse response = authService.refreshToken(refreshTokenRequest);

        assertNotNull(response);
        assertEquals(TestConstants.NEW_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestConstants.NEW_REFRESH_TOKEN, response.getRefreshToken());

        verify(jwtService).extractUsername(refreshTokenRequest.getRefreshToken());
        verify(jwtService).getRoles(refreshTokenRequest.getRefreshToken());
        verify(jwtService).deleteRefreshTokenByUserEmail(testUser.getUsername());
        verify(jwtService).generateAccessToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).generateRefreshToken(testUser.getUsername(), List.of(TestConstants.ROLE_USER));
        verify(jwtService).saveRefreshToken(TestConstants.NEW_REFRESH_TOKEN);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        when(jwtService.isTokenValid(anyString())).thenReturn(true);

        boolean isValid = authService.validateToken(TestConstants.VALID_TOKEN);

        assertTrue(isValid);
        verify(jwtService).isTokenValid(TestConstants.VALID_TOKEN);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        boolean isValid = authService.validateToken(TestConstants.INVALID_TOKEN);

        assertFalse(isValid);
        verify(jwtService).isTokenValid(TestConstants.INVALID_TOKEN);
    }

    @Test
    void deleteUserCredential_ShouldDeleteUserAndRefreshToken_WhenUserExists() {
        when(userCredentialRepository.findById(TestConstants.USER_ID)).thenReturn(Optional.of(testUser));

        authService.deleteUserCredential(TestConstants.USER_ID);

        verify(jwtService).deleteRefreshTokenByUserEmail(testUser.getEmail());
        verify(userCredentialRepository, times(1)).deleteById(TestConstants.USER_ID);
        verify(userCredentialRepository, times(1)).findById(TestConstants.USER_ID);
    }

    @Test
    void deleteUserCredential_ShouldThrowException_WhenUserNotFound() {
        when(userCredentialRepository.findById(TestConstants.SECOND_USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserCredentialNotFoundException.class,
                () -> authService.deleteUserCredential(TestConstants.SECOND_USER_ID));

        verify(userCredentialRepository, never()).deleteById(any());
        verify(userCredentialRepository, times(1)).findById(TestConstants.SECOND_USER_ID);
    }
}
