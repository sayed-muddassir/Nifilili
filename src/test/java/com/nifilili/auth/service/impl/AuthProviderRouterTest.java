package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.service.AuthProviderStrategy;
import com.nifilili.core.exception.UnsupportedAuthMethodException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthProviderRouterTest {

    @Mock private AuthProviderStrategy emailProvider;
    @Mock private AuthProviderStrategy phoneProvider;

    private AuthProviderRouter router;

    @BeforeEach
    void setUp() {
        router = new AuthProviderRouter(List.of(emailProvider, phoneProvider));
    }

    // ── authenticate ──────────────────────────────────────────────────────

    @Test
    void authenticate_WhenMatchingProviderExists_ShouldDelegateToIt() {
        AuthRequest request = new AuthRequest();
        request.setAuthType(AuthType.EMAIL_PASSWORD);

        AuthResult expected = new AuthResult(1L, "john");

        when(emailProvider.supports(AuthType.EMAIL_PASSWORD)).thenReturn(true);
        when(emailProvider.authenticate(request, "ip", "ua", "device")).thenReturn(expected);

        AuthResult result = router.authenticate(request, "ip", "ua", "device");

        assertEquals(1L, result.getUserId());
        assertEquals("john", result.getUsername());
        verify(emailProvider).authenticate(request, "ip", "ua", "device");
        verify(phoneProvider, never()).authenticate(any(), any(), any(), any());
    }

    @Test
    void authenticate_WhenNoMatchingProvider_ShouldThrowUnsupportedAuthMethodException() {
        AuthRequest request = new AuthRequest();
        request.setAuthType(AuthType.GOOGLE);

        when(emailProvider.supports(AuthType.GOOGLE)).thenReturn(false);
        when(phoneProvider.supports(AuthType.GOOGLE)).thenReturn(false);

        assertThrows(UnsupportedAuthMethodException.class,
                () -> router.authenticate(request, "ip", "ua", "device"));
    }

    // ── register ──────────────────────────────────────────────────────────

    @Test
    void register_WhenMatchingProviderExists_ShouldDelegateToIt() {
        RegistrationRequest request = new RegistrationRequest();
        request.setAuthType(AuthType.PHONE_OTP);

        AuthResult expected = new AuthResult(2L, "phoneuser");

        when(emailProvider.supports(AuthType.PHONE_OTP)).thenReturn(false);
        when(phoneProvider.supports(AuthType.PHONE_OTP)).thenReturn(true);
        when(phoneProvider.register(request, "ip", "ua", "device")).thenReturn(expected);

        AuthResult result = router.register(request, "ip", "ua", "device");

        assertEquals(2L, result.getUserId());
        verify(phoneProvider).register(request, "ip", "ua", "device");
        verify(emailProvider, never()).register(any(), any(), any(), any());
    }

    @Test
    void register_WhenNoMatchingProvider_ShouldThrowUnsupportedAuthMethodException() {
        RegistrationRequest request = new RegistrationRequest();
        request.setAuthType(AuthType.FACEBOOK);

        when(emailProvider.supports(AuthType.FACEBOOK)).thenReturn(false);
        when(phoneProvider.supports(AuthType.FACEBOOK)).thenReturn(false);

        assertThrows(UnsupportedAuthMethodException.class,
                () -> router.register(request, "ip", "ua", "device"));
    }
}
