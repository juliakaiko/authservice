package com.mymicroservice.authservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.authservice.configuration.SecurityConfig;
import com.mymicroservice.authservice.controller.InternalController;
import com.mymicroservice.authservice.filter.GatewayAuthFilter;
import com.mymicroservice.authservice.service.AuthService;
import com.mymicroservice.authservice.util.data.TestConstants;
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
class InternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private GatewayAuthFilter gatewayAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deleteUser_ShouldSucceed_WhenInternalCallHeaderIsPresent() throws Exception {
        doNothing().when(authService).deleteUserCredential(TestConstants.USER_ID);

        mockMvc.perform(delete("/api/internal/auth/user/{id}", TestConstants.USER_ID)
                        .header(TestConstants.INTERNAL_CALL_HEADER, TestConstants.INTERNAL_CALL_TRUE))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).deleteUserCredential(TestConstants.USER_ID);
    }

    @Test
    void deleteUser_ShouldBeForbidden_WhenInternalCallHeaderIsMissing() throws Exception {
        mockMvc.perform(delete("/api/internal/auth/user/{id}", TestConstants.USER_ID))
                .andExpect(status().isForbidden());

        verify(authService, never()).deleteUserCredential(anyLong());
    }

    @Test
    void deleteUser_ShouldBeForbidden_WhenInternalCallHeaderIsIncorrect() throws Exception {
        mockMvc.perform(delete("/api/internal/auth/user/{id}", TestConstants.USER_ID)
                        .header(TestConstants.INTERNAL_CALL_HEADER, TestConstants.INTERNAL_CALL_FALSE))
                .andExpect(status().isForbidden());

        verify(authService, never()).deleteUserCredential(anyLong());
    }
}
