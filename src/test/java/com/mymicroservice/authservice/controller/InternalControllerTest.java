package com.mymicroservice.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.authservice.configuration.SecurityConfig;
import com.mymicroservice.authservice.filter.GatewayAuthFilter;
import com.mymicroservice.authservice.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class InternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private GatewayAuthFilter gatewayAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("DELETE /api/internal/auth/user/{id} should delete user when internal call header is present")
    void deleteUserInternalCallShouldSucceed() throws Exception {
        Long userId = 1L;

        doNothing().when(authService).deleteUserCredential(userId);

        mockMvc.perform(delete("/api/internal/auth/user/{id}", userId)
                        .header("X-Internal-Call", "true"))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).deleteUserCredential(userId);
    }

    @Test
    @DisplayName("DELETE /api/internal/auth/user/{id} should be forbidden without internal call header")
    void deleteUserWithoutInternalCallShouldBeForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/internal/auth/user/{id}", userId))
                .andExpect(status().isForbidden());

        verify(authService, never()).deleteUserCredential(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/internal/auth/user/{id} should be forbidden with incorrect internal call header")
    void deleteUserWithIncorrectInternalCallShouldBeForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/internal/auth/user/{id}", userId)
                        .header("X-Internal-Call", "false")) // неправильное значение
                .andExpect(status().isForbidden());

        verify(authService, never()).deleteUserCredential(anyLong());
    }
}
