package com.mymicroservice.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.authservice.configuration.SecurityConfig;
import com.mymicroservice.authservice.dto.AuthRequest;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.dto.RefreshTokenRequest;
import com.mymicroservice.authservice.dto.UserRegistrationRequest;
import com.mymicroservice.authservice.mapper.UserCredentialMapper;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.security.JwtAuthFilter;
import com.mymicroservice.authservice.service.AuthService;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /auth/register should return AuthResponse")
    void registerShouldReturnAuthResponse() throws Exception {
        UserCredential userCredential = UserCredentialGenerator.generateUser();
        UserRegistrationRequest request = UserCredentialMapper.INSTANSE.toDto(userCredential);
        AuthResponse response = new AuthResponse("access-token", "refresh-token");

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /auth/login should return AuthResponse")
    void loginShouldReturnAuthResponse() throws Exception {
        AuthRequest request = new AuthRequest("testuser@test.by", "testpass");
        AuthResponse response = new AuthResponse("access-token", "refresh-token");

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /auth/refresh should return new AuthResponse")
    void refreshShouldReturnAuthResponse() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
        AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /auth/validate should return true for valid token")
    void validateShouldReturnTrue() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(post("/auth/validate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /auth/validate should return false for invalid token")
    void validateShouldReturnFalse() throws Exception {
        when(authService.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(post("/auth/validate")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
