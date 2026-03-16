package com.nifilili.auth.service.impl;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.domain.enums.ProviderType;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.events.LoginAttemptEvent;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.AuthProviderStrategy;
import com.nifilili.auth.service.LoginAttemptService;
import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.core.exception.AccountLockedException;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Handles email/username + password authentication and registration.
 * Refactored from the original AuthServiceImpl login/register logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailPasswordAuthProvider implements AuthProviderStrategy {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean supports(AuthType authType) {
        return AuthType.EMAIL_PASSWORD == authType;
    }

    @Override
    @Transactional
    public AuthResult authenticate(AuthRequest request, String ipAddress, String userAgent, String deviceName) {
        String usernameOrEmail = request.getUsernameOrEmail();
        log.debug("Email/password auth for '{}'", usernameOrEmail);

        if (loginAttemptService.isAccountLocked(usernameOrEmail)) {
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Please reset your password.");
        }

        Long userId = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(User::getId)
                .orElse(null);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, request.getPassword()));
        } catch (BadCredentialsException ex) {
            eventPublisher.publishEvent(new LoginAttemptEvent(userId, usernameOrEmail, ipAddress, false));
            throw ex;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        eventPublisher.publishEvent(new LoginAttemptEvent(principal.getUserId(), usernameOrEmail, ipAddress, true));
        recordLoginHistory(principal.getUserId(), ipAddress, userAgent, deviceName);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Email/password login succeeded for userId={}", principal.getUserId());
        return AuthResult.builder()
                .userId(principal.getUserId())
                .username(principal.getUsername())
                .build();
    }

    @Override
    @Transactional
    public AuthResult register(RegistrationRequest request, String ipAddress, String userAgent, String deviceName) {
        log.debug("Email/password registration for email='{}'", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required for EMAIL_PASSWORD registration");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for EMAIL_PASSWORD registration");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email address is already registered");
        }

        String username = resolveUsername(request);
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username is already taken: " + username);
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not seeded in database"));

        User newUser = User.builder()
                .name(request.getName())
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .emailVerified(false)
                .phoneVerified(false)
                .accountLocked(false)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(newUser);
        log.info("Registered user id={} via EMAIL_PASSWORD", saved.getId());

        // Track linked identity
        authProviderRepository.save(UserAuthProvider.builder()
                .userId(saved.getId())
                .providerType(ProviderType.LOCAL_EMAIL)
                .linkedAt(LocalDateTime.now())
                .build());

        // Authenticate for token generation
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        recordLoginHistory(saved.getId(), ipAddress, userAgent, deviceName);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail()));

        return AuthResult.builder()
                .userId(saved.getId())
                .username(saved.getUsername())
                .build();
    }

    private String resolveUsername(RegistrationRequest request) {
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return request.getUsername();
        }
        // Auto-generate from name: "John Doe" → "johndoe" + random suffix
        String base = request.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.length() < 3) {
            base = base + "user";
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private void recordLoginHistory(Long userId, String ipAddress, String userAgent, String deviceName) {
        loginHistoryRepository.save(LoginHistory.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceName(deviceName)
                .loggedInAt(LocalDateTime.now())
                .build());
    }
}
