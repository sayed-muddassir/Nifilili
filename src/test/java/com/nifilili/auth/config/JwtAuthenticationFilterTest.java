package com.nifilili.auth.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WhenBearerTokenValid_ShouldSetAuthentication() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        UserDetails details = new User("john", "pwd", List.of());
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsername("valid-token")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(details);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(details, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(userDetailsService).loadUserByUsername("john");
    }

    @Test
    void doFilterInternal_WhenAuthorizationHeaderMissing_ShouldSkipAuthentication() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }

    @Test
    void doFilterInternal_WhenTokenProcessingFails_ShouldClearContextAndContinue() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders");
        request.addHeader("Authorization", "Bearer broken-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtTokenProvider.validateToken("broken-token")).thenReturn(true);
        when(jwtTokenProvider.getUsername("broken-token")).thenThrow(new RuntimeException("parse failure"));

        filter.doFilter(request, response, chain);

        // Filter must not break the request chain on JWT parsing failure.
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }
}

