package com.mymicroservice.authservice.service;

import com.mymicroservice.authservice.model.RefreshToken;
import com.mymicroservice.authservice.repositiry.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService  {

    @Value("${jwt.expiration}")
    private Duration jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Duration refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private static final String PRIVATE_KEY_PATH = "keys/private.pem"; //src/main/resources/
    private static final String PUBLIC_KEY_PATH = "keys/public.pem"; // src/main/resources/

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        this.privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
        this.publicKey = loadPublicKey(PUBLIC_KEY_PATH);
    }

    public List<String> getRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    //Extract all data from a token
    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token)
                .getBody();
    }

    public String generateAccessToken(String username, List<String> roles) {
        log.info("generateAccessToken(): {}",username);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration.toMillis()))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(String username,List<String> roles) {
        log.info("generateRefreshToken(): {}",username);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration.toMillis()))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @Transactional
    public void saveRefreshToken(String refreshToken) {
        log.info("saveRefreshToken(): {}",refreshToken);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String username = claims.getSubject();
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        // Checking if a token exists for this user
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserEmailIgnoreCase(username);

        if (existingToken.isPresent()) {
            // If the token already exists, update it
            RefreshToken tokenToUpdate = existingToken.get();
            tokenToUpdate.setRefreshToken(refreshToken);
            tokenToUpdate.setIssuedAt(issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            tokenToUpdate.setExpiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            refreshTokenRepository.save(tokenToUpdate);
        } else {
            // If there is no token, create a new one
            RefreshToken tokenEntity = new RefreshToken();
            tokenEntity.setUserEmail(username);
            tokenEntity.setRefreshToken(refreshToken);
            tokenEntity.setIssuedAt(issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            tokenEntity.setExpiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            refreshTokenRepository.save(tokenEntity);
        }
    }

    @Transactional
    public void deleteRefreshTokenByUserEmail (String email){
        refreshTokenRepository.deleteRefreshTokenByUserEmailIgnoreCase(email);
        log.info("deleteRefreshTokenByUserEmail(): {}",email);
    }

    /**
     * Checks if JWT is valid by verifying:
     * 1. Signature (not tampered)
     * 2. Expiration date (not expired)
     * 3. Basic structure (proper JWT format)
     *
     * @param token JWT to validate
     * @return true if valid, false if invalid/expired
     */
    public boolean isTokenValid(String token) {
        log.info("isTokenValid(): {}",token);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        log.info("extractUsername(): {}",token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    private PrivateKey loadPrivateKey(String classpathPath) {
        try (InputStream inputStream = new ClassPathResource(classpathPath).getInputStream()) {
            String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key from: " + classpathPath, e);
        }
    }

    private PublicKey loadPublicKey(String classpathPath) {
        try (InputStream inputStream = new ClassPathResource(classpathPath).getInputStream()) {
            String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key from: " + classpathPath, e);
        }
    }


  /*  private PrivateKey loadPrivateKey(String filepath) {
        try {
            String key = new String(Files.readAllBytes(Paths.get(filepath)))
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private PublicKey loadPublicKey(String filepath) {
        try {
            String key = new String(Files.readAllBytes(Paths.get(filepath)))
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }*/
}
