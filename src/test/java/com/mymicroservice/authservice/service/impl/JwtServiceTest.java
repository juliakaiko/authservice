package com.mymicroservice.authservice.service.impl;

import com.mymicroservice.authservice.model.RefreshToken;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.repositiry.RefreshTokenRepository;
import com.mymicroservice.authservice.service.JwtService;
import com.mymicroservice.authservice.util.RefreshTokenGenerator;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private static final String TEST_USER_EMAIL = "test@test.by";

    @BeforeEach
    void setUp() throws Exception {
        // Generate temporary RSA keys for tests
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        // Installing the keys manually
        var privateField = JwtService.class.getDeclaredField("privateKey");
        var publicField = JwtService.class.getDeclaredField("publicKey");
        privateField.setAccessible(true);
        publicField.setAccessible(true);
        privateField.set(jwtService, privateKey);
        publicField.set(jwtService, publicKey);

        // Setting the expiration values manually
        var expirationField = JwtService.class.getDeclaredField("jwtExpiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, Duration.ofMinutes(15));

        var refreshExpField = JwtService.class.getDeclaredField("refreshExpiration");
        refreshExpField.setAccessible(true);
        refreshExpField.set(jwtService, Duration.ofDays(7));
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String token = jwtService.generateAccessToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(TEST_USER_EMAIL, jwtService.extractUsername(token));
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(TEST_USER_EMAIL, jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.string";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateAccessToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertEquals(TEST_USER_EMAIL, jwtService.extractUsername(token));
    }

    @Test
    void saveRefreshToken_WhenTokenExists_ShouldUpdateToken() {
        String refreshToken = jwtService.generateRefreshToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));
        RefreshToken existingToken = RefreshTokenGenerator.generateRefreshToken();

        when(refreshTokenRepository.findByUserEmailIgnoreCase(TEST_USER_EMAIL))
                .thenReturn(Optional.of(existingToken));

        jwtService.saveRefreshToken(refreshToken);

        verify(refreshTokenRepository, times(1)).save(existingToken);
    }

    @Test
    void saveRefreshToken_WhenTokenNotExists_ShouldCreateNewToken() {
        String refreshToken = jwtService.generateRefreshToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        when(refreshTokenRepository.findByUserEmailIgnoreCase(TEST_USER_EMAIL))
                .thenReturn(Optional.empty());

        jwtService.saveRefreshToken(refreshToken);

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void deleteRefreshTokenByUserEmail_ShouldCallRepository() {
        jwtService.deleteRefreshTokenByUserEmail(TEST_USER_EMAIL);

        verify(refreshTokenRepository).deleteRefreshTokenByUserEmailIgnoreCase(TEST_USER_EMAIL);
    }

    @Test
    void extractAllClaims_ShouldReturnAllClaims() {
        String token = jwtService.generateAccessToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals(TEST_USER_EMAIL, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getRoles_ShouldReturnRolesFromToken() {
        String token = jwtService.generateAccessToken(TEST_USER_EMAIL, List.of(Role.USER.getAuthority()));

        List <String> roles = jwtService.getRoles(token);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }
}
