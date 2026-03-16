package com.nifilili.auth.service;

import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;

/**
 * Strategy interface for pluggable authentication providers.
 * <p>
 * Each implementation handles a specific {@link AuthType} (email+password, phone+OTP,
 * social login, etc.). New providers are added by implementing this interface and
 * registering as a Spring bean — no changes to controllers or the router.
 */
public interface AuthProviderStrategy {

    /**
     * Returns whether this provider handles the given authentication type.
     *
     * @param authType the authentication method being requested
     * @return {@code true} if this provider can handle the request
     */
    boolean supports(AuthType authType);

    /**
     * Authenticates an existing user.
     *
     * @param request credentials or token data
     * @param ipAddress client IP for audit
     * @param userAgent client user-agent for device tracking
     * @param deviceName device label
     * @return authentication result containing user ID and metadata
     */
    AuthResult authenticate(AuthRequest request, String ipAddress, String userAgent, String deviceName);

    /**
     * Registers a new user via this provider.
     *
     * @param request registration data
     * @param ipAddress client IP for audit
     * @param userAgent client user-agent for device tracking
     * @param deviceName device label
     * @return authentication result for the newly registered user
     */
    AuthResult register(RegistrationRequest request, String ipAddress, String userAgent, String deviceName);
}
