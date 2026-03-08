package com.nifilili.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationEntryPointTest {

    @Test
    void commence_ShouldReturnUnauthorizedJsonPayload() throws Exception {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getHandler(any())).thenReturn(new HandlerExecutionChain(new Object()));
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(handlerMapping, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new AuthenticationException("Bad credentials") {
                }
        );

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"error\":\"Unauthorized\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/api/v1/orders\""));
        assertTrue(response.getContentAsString().contains("\"message\":\"Authentication is required to access this resource\""));
    }

    @Test
    void commence_WhenNoHandlerExists_ShouldReturnNotFoundJsonPayload() throws Exception {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getHandler(any())).thenReturn(null);
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(handlerMapping, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/unknown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new AuthenticationException("Bad credentials") {
                }
        );

        assertEquals(404, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"error\":\"Not Found\""));
        assertTrue(response.getContentAsString().contains("\"message\":\"URL not found\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/api/v1/unknown\""));
    }
}
