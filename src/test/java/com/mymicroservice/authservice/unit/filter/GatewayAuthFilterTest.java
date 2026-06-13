package com.mymicroservice.authservice.unit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.authservice.filter.GatewayAuthFilter;
import com.mymicroservice.authservice.util.CommonConstants;
import com.mymicroservice.authservice.util.data.TestConstants;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GatewayAuthFilterTest {

    private GatewayAuthFilter gatewayAuthFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        gatewayAuthFilter = new GatewayAuthFilter(new ObjectMapper());
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_ShouldSetSecurityContext_WhenGatewayCallHasValidJwt() throws Exception {
        MockHttpServletRequest request = createGatewayRequest();
        request.addHeader("Authorization", "Bearer " + buildJwt(TestConstants.USER_EMAIL, List.of(TestConstants.ROLE_USER)));
        MockHttpServletResponse response = new MockHttpServletResponse();

        gatewayAuthFilter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(TestConstants.USER_EMAIL, authentication.getPrincipal());
        assertEquals(1, authentication.getAuthorities().size());
        assertEquals("ROLE_" + TestConstants.ROLE_USER, authentication.getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldClearSecurityContext_WhenInternalCallDetected() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.INTERNAL_CALL_HEADER, TestConstants.INTERNAL_CALL_TRUE);
        request.addHeader(CommonConstants.SOURCE_SERVICE_HEADER, "userservice");
        MockHttpServletResponse response = new MockHttpServletResponse();

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldNotSetSecurityContext_WhenBearerTokenIsMissing() throws Exception {
        MockHttpServletRequest request = createGatewayRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldNotSetSecurityContext_WhenJwtStructureIsInvalid() throws Exception {
        MockHttpServletRequest request = createGatewayRequest();
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldClearSecurityContext_WhenSourceServiceHeaderIsWrong() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.INTERNAL_CALL_HEADER, TestConstants.INTERNAL_CALL_TRUE);
        request.addHeader(CommonConstants.SOURCE_SERVICE_HEADER, "wrong-service");
        request.addHeader("Authorization", "Bearer " + buildJwt(TestConstants.USER_EMAIL, List.of(TestConstants.ROLE_USER)));
        MockHttpServletResponse response = new MockHttpServletResponse();

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldContinueChain_WhenJwtPayloadParsingFails() throws Exception {
        MockHttpServletRequest request = createGatewayRequest();
        request.addHeader("Authorization", "Bearer header.!!!invalid-base64!!!.signature");
        MockHttpServletResponse response = new MockHttpServletResponse();

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest createGatewayRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.INTERNAL_CALL_HEADER, TestConstants.INTERNAL_CALL_TRUE);
        request.addHeader(CommonConstants.SOURCE_SERVICE_HEADER, CommonConstants.GATEWAY_SERVICE_NAME);
        return request;
    }

    private String buildJwt(String subject, List<String> roles) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", subject);
        payload.put("roles", roles);
        String payloadJson = mapper.writeValueAsString(payload);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "header." + encodedPayload + ".signature";
    }
}
