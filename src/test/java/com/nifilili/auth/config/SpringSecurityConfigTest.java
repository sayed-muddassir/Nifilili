package com.nifilili.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringSecurityConfigTest {

    @Test
    void passwordEncoder_ShouldEncodeAndMatchPassword() {
        PasswordEncoder encoder = SpringSecurityConfig.passwordEncoder();

        String encoded = encoder.encode("my-secret");
        assertNotEquals("my-secret", encoded);
        assertTrue(encoder.matches("my-secret", encoded));
        assertFalse(encoder.matches("wrong", encoded));
    }

    @Test
    void authenticationManager_ShouldReturnManagerFromConfiguration() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = mock(JwtAuthenticationEntryPoint.class);
        JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);
        SpringSecurityConfig config = new SpringSecurityConfig(entryPoint, filter);

        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = config.authenticationManager(authConfig);

        assertSame(manager, result);
    }

    @Test
    void securityFilterChain_MethodShouldExistForFrameworkWiring() throws Exception {
        Method method = SpringSecurityConfig.class.getDeclaredMethod(
                "securityFilterChain",
                org.springframework.security.config.annotation.web.builders.HttpSecurity.class
        );

        assertNotNull(method);
        assertEquals("securityFilterChain", method.getName());
    }
}

