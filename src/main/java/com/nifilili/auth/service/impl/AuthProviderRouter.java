package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.service.AuthProviderStrategy;
import com.nifilili.core.exception.UnsupportedAuthMethodException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Routes authentication and registration requests to the appropriate
 * {@link AuthProviderStrategy} implementation based on the requested {@link AuthType}.
 * <p>
 * New providers are auto-discovered via Spring's component scanning — simply implement
 * {@link AuthProviderStrategy} and register as a bean.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthProviderRouter {

    private final List<AuthProviderStrategy> providers;

    /**
     * Routes an authentication request to the matching provider.
     *
     * @param request    credentials/token data
     * @param ipAddress  client IP
     * @param userAgent  client user-agent
     * @param deviceName device label
     * @return authentication result
     * @throws UnsupportedAuthMethodException if no provider supports the auth type
     */
    public AuthResult authenticate(AuthRequest request, String ipAddress, String userAgent, String deviceName) {
        AuthType authType = request.getAuthType();
        log.debug("Routing auth request: authType={}", authType);

        return findProvider(authType).authenticate(request, ipAddress, userAgent, deviceName);
    }

    /**
     * Routes a registration request to the matching provider.
     *
     * @param request    registration data
     * @param ipAddress  client IP
     * @param userAgent  client user-agent
     * @param deviceName device label
     * @return authentication result for the new user
     * @throws UnsupportedAuthMethodException if no provider supports the auth type
     */
    public AuthResult register(RegistrationRequest request, String ipAddress, String userAgent, String deviceName) {
        AuthType authType = request.getAuthType();
        log.debug("Routing registration request: authType={}", authType);

        return findProvider(authType).register(request, ipAddress, userAgent, deviceName);
    }

    private AuthProviderStrategy findProvider(AuthType authType) {
        return providers.stream()
                .filter(p -> p.supports(authType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedAuthMethodException(
                        "Authentication method not supported: " + authType));
    }
}
