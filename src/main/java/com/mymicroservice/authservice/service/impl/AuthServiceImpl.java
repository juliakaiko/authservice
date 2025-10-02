package com.mymicroservice.authservice.service.impl;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.exception.InvalidCredentialsException;
import com.mymicroservice.authservice.exception.UserCredentialNotFoundException;
import com.mymicroservice.authservice.mapper.UserCredentialMapper;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.service.JwtService;
import com.mymicroservice.authservice.repositiry.UserCredentialRepository;
import com.mymicroservice.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {

        if (userCredentialRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        UserCredential user;
        String roleAuthority = request.getRole() != null ? request.getRole().getAuthority() : null;

        if (roleAuthority == null || roleAuthority.equals("USER")) {
            user = UserCredentialMapper.INSTANSE.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.USER);
            log.info("Request to add new USER: {}", user.getEmail());
        } else if (roleAuthority.equals("ADMIN")) {
            user = UserCredentialMapper.INSTANSE.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.ADMIN);
            log.info("Request to add new ADMIN: {}", user.getEmail());
        } else {
            throw new IllegalArgumentException("Unknown or unsupported role: " + roleAuthority);
        }

        userCredentialRepository.save(user);

        String access = jwtService.generateAccessToken(user.getUsername(), List.of(user.getRole().getAuthority()));
        String refresh = jwtService.generateRefreshToken(user.getUsername(), List.of(user.getRole().getAuthority()));
        jwtService.saveRefreshToken(refresh); // save refreshToken in DB

        return new AuthResponse(access, refresh);
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Request to authenticate user: {}",request.getEmail());

        UserCredential user = userCredentialRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Incorrect email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw new InvalidCredentialsException("Incorrect email or password");
        }

        String access = jwtService.generateAccessToken(user.getUsername(), List.of(user.getRole().getAuthority()));
        String refresh = jwtService.generateRefreshToken(user.getUsername(), List.of(user.getRole().getAuthority()));
        jwtService.saveRefreshToken(refresh); // save refreshToken in DB

        return new AuthResponse(access, refresh);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());
        List<String> roles = jwtService.getRoles(request.getRefreshToken());
        jwtService.deleteRefreshTokenByUserEmail(username);

        String access = jwtService.generateAccessToken(username, roles);
        String refresh = jwtService.generateRefreshToken(username, roles);
        jwtService.saveRefreshToken(refresh);

        return new AuthResponse(access, refresh);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }

    @Override
    @Transactional
    public void deleteUserCredential(Long userId) {
        Optional<UserCredential> userFromDb = Optional.ofNullable(userCredentialRepository.findById(userId)
                .orElseThrow(() -> new UserCredentialNotFoundException("UserCredential wasn't found with id " + userId)));
        userCredentialRepository.deleteById(userId);
        log.info("deleteUserCredential(): {}", userFromDb);
    }
}
