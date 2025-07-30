package com.mymicroservice.authservice.service.impl;

import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.exception.InvalidCredentialsException;
import com.mymicroservice.authservice.model.Role;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.service.JwtService;
import com.mymicroservice.authservice.repositiry.UserCredentialRepository;
import com.mymicroservice.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService { //UserDetailsService
    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(AuthRequest request) {

        if (repository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        UserCredential user;
        String roleAuthority = request.getRole() != null ? request.getRole().getAuthority() : null;

        if (roleAuthority == null || roleAuthority.equals("USER")) {
            user = UserCredential.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .build();
            log.info("Request to add new USER: {}", user.getEmail());
        } else if (roleAuthority.equals("ADMIN")) {
            user = UserCredential.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.ADMIN)
                    .build();
            log.info("Request to add new ADMIN: {}", user.getEmail());
        } else {
            throw new IllegalArgumentException("Unknown or unsupported role: " + roleAuthority);
        }

        repository.save(user);

        String access = jwtService.generateToken(user.getUsername());
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, refresh);
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Request to authenticate user: {}",request.getEmail());
        //var user = loadUserByUsername(request.getEmail())

        UserCredential user = repository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Incorrect email")); //Incorrect email or password

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw new InvalidCredentialsException("Incorrect password"); //Incorrect email or password
        }

        String access = jwtService.generateToken(user.getUsername());
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, refresh);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());
        String access = jwtService.generateToken(username);
        String refresh = jwtService.generateRefreshToken(username);
        return new AuthResponse(access, refresh);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }
/*
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + username + " не найден"));
    }*/

 /*   @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredential userFromDb = null;
        Optional<UserCredential> userCredential = repository.findByEmailIgnoreCase(username);
        if (userCredential.isPresent()){
            userFromDb = userCredential.get();
        }

        Optional <UserCredential> optionalUser = Optional.ofNullable(userFromDb);
        return optionalUser.map(userFromRequest -> new org.springframework.security.core.userdetails.User(
                optionalUser.get().getEmail(),
                optionalUser.get().getPassword(),
                Collections.singleton(optionalUser.get().getRole())
        )).orElseThrow(() -> new UsernameNotFoundException(username+ " not found"));
    }*/

}
