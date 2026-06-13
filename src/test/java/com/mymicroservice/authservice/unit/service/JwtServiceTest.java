package com.mymicroservice.authservice.unit.service;

import com.mymicroservice.authservice.model.RefreshToken;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.repository.RefreshTokenRepository;
import com.mymicroservice.authservice.service.JwtService;
import com.mymicroservice.authservice.util.RefreshTokenGenerator;
import com.mymicroservice.authservice.util.data.TestConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(TestConstants.RSA_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        var privateField = JwtService.class.getDeclaredField("privateKey");
        var publicField = JwtService.class.getDeclaredField("publicKey");
        privateField.setAccessible(true);
        publicField.setAccessible(true);
        privateField.set(jwtService, privateKey);
        publicField.set(jwtService, publicKey);

        var expirationField = JwtService.class.getDeclaredField("jwtExpiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, Duration.ofMinutes(15));

        var refreshExpField = JwtService.class.getDeclaredField("refreshExpiration");
        refreshExpField.setAccessible(true);
        refreshExpField.set(jwtService, Duration.ofDays(7));
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken_WhenUserEmailProvided() {
        String token = jwtService.generateAccessToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(TestConstants.USER_EMAIL, jwtService.extractUsername(token));
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken_WhenUserEmailProvided() {
        String token = jwtService.generateRefreshToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(TestConstants.USER_EMAIL, jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtService.generateAccessToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsInvalid() {
        assertFalse(jwtService.isTokenValid(TestConstants.INVALID_JWT_STRING));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenTokenIsValid() {
        String token = jwtService.generateAccessToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        assertEquals(TestConstants.USER_EMAIL, jwtService.extractUsername(token));
    }

    @Test
    void saveRefreshToken_ShouldUpdateToken_WhenTokenExists() {
        String refreshToken = jwtService.generateRefreshToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));
        RefreshToken existingToken = RefreshTokenGenerator.generateRefreshToken();

        when(refreshTokenRepository.findByUserEmailIgnoreCase(TestConstants.USER_EMAIL))
                .thenReturn(Optional.of(existingToken));

        jwtService.saveRefreshToken(refreshToken);

        verify(refreshTokenRepository, times(1)).save(existingToken);
    }

    @Test
    void saveRefreshToken_ShouldCreateNewToken_WhenTokenNotExists() {
        String refreshToken = jwtService.generateRefreshToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        when(refreshTokenRepository.findByUserEmailIgnoreCase(TestConstants.USER_EMAIL))
                .thenReturn(Optional.empty());

        jwtService.saveRefreshToken(refreshToken);

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void deleteRefreshTokenByUserEmail_ShouldCallRepository_WhenEmailProvided() {
        jwtService.deleteRefreshTokenByUserEmail(TestConstants.USER_EMAIL);

        verify(refreshTokenRepository).deleteRefreshTokenByUserEmailIgnoreCase(TestConstants.USER_EMAIL);
    }

    @Test
    void extractAllClaims_ShouldReturnAllClaims_WhenTokenIsValid() {
        String token = jwtService.generateAccessToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals(TestConstants.USER_EMAIL, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getRoles_ShouldReturnRolesFromToken_WhenTokenIsValid() {
        String token = jwtService.generateAccessToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));

        List<String> roles = jwtService.getRoles(token);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals(TestConstants.ROLE_USER, roles.get(0));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        Instant issuedAt = Instant.parse("2020-01-01T00:00:00Z");
        Instant expiresAt = Instant.parse("2020-01-01T00:00:01Z");
        String token = Jwts.builder()
                .setSubject(TestConstants.USER_EMAIL)
                .claim("roles", List.of(Role.USER.getAuthority()))
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenSignedWithDifferentKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(TestConstants.RSA_KEY_SIZE);
        KeyPair otherKeyPair = keyGen.generateKeyPair();

        Instant now = Instant.parse("2025-01-01T12:00:00Z");
        String foreignToken = Jwts.builder()
                .setSubject(TestConstants.USER_EMAIL)
                .claim("roles", List.of(Role.USER.getAuthority()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(60)))
                .signWith(otherKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertFalse(jwtService.isTokenValid(foreignToken));
    }

    @Test
    void init_ShouldLoadKeysFromClasspath_WhenApplicationStarts() throws Exception {
        JwtService service = new JwtService(refreshTokenRepository);
        service.init();

        setDurationField(service, "jwtExpiration", Duration.ofMinutes(15));
        setDurationField(service, "refreshExpiration", Duration.ofDays(7));

        String token = service.generateAccessToken(TestConstants.USER_EMAIL, List.of(Role.USER.getAuthority()));
        assertTrue(service.isTokenValid(token));
    }

    private void setDurationField(JwtService service, String fieldName, Duration value) throws Exception {
        var field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
