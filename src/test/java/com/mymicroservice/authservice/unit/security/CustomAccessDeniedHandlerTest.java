package com.mymicroservice.authservice.unit.security;

import com.mymicroservice.authservice.security.CustomAccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Test
    void handle_ShouldReturnForbiddenJson_WhenAccessIsDenied() throws Exception {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");

        handler.handle(request, response, accessDeniedException);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\": 403"));
        assertTrue(body.contains("\"error\": \"Forbidden\""));
        assertTrue(body.contains("\"message\": \"You don't have rights to perform this action.\""));
    }
}
