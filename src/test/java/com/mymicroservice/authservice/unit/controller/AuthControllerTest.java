package com.mymicroservice.authservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.authservice.configuration.SecurityConfig;
import com.mymicroservice.authservice.controller.AuthController;
import com.mymicroservice.authservice.dto.AuthResponse;
import com.mymicroservice.authservice.filter.GatewayAuthFilter;
import com.mymicroservice.authservice.service.AuthService;
import com.mymicroservice.authservice.util.AuthRequestGenerator;
import com.mymicroservice.authservice.util.data.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private GatewayAuthFilter gatewayAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnAuthResponse_WhenRequestIsValid() throws Exception {
        AuthResponse response = new AuthResponse(TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN);

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AuthRequestGenerator.generateRegistrationRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(TestConstants.ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(TestConstants.REFRESH_TOKEN));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() throws Exception {
        AuthResponse response = new AuthResponse(TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN);

        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AuthRequestGenerator.generateAuthRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(TestConstants.ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(TestConstants.REFRESH_TOKEN));
    }

    @Test
    void refresh_ShouldReturnAuthResponse_WhenRefreshTokenIsValid() throws Exception {
        AuthResponse response = new AuthResponse(TestConstants.NEW_ACCESS_TOKEN, TestConstants.NEW_REFRESH_TOKEN);

        when(authService.refreshToken(any())).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AuthRequestGenerator.generateRefreshTokenRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(TestConstants.NEW_ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(TestConstants.NEW_REFRESH_TOKEN));
    }

    @Test
    void validate_ShouldReturnTrue_WhenTokenIsValid() throws Exception {
        when(authService.validateToken(TestConstants.VALID_TOKEN)).thenReturn(true);

        mockMvc.perform(post("/auth/validate")
                        .param("token", TestConstants.VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validate_ShouldReturnFalse_WhenTokenIsInvalid() throws Exception {
        when(authService.validateToken(TestConstants.INVALID_TOKEN)).thenReturn(false);

        mockMvc.perform(post("/auth/validate")
                        .param("token", TestConstants.INVALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
