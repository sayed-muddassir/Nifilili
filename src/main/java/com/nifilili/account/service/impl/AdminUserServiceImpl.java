package com.nifilili.account.service.impl;

import com.nifilili.account.dto.request.AdminCreateUserRequest;
import com.nifilili.account.dto.request.AdminUpdateRolesRequest;
import com.nifilili.account.dto.request.AdminUpdateUserStatusRequest;
import com.nifilili.account.dto.response.AdminUserResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.events.AdminUserCreatedEvent;
import com.nifilili.account.service.AdminUserService;
import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.ProviderType;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.PhoneAlreadyExistsException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(String role, Boolean enabled, Boolean accountLocked,
                                             String search, Pageable pageable) {
        log.debug("Admin listing users: role={}, enabled={}, locked={}, search='{}'",
                role, enabled, accountLocked, search);

        Specification<User> spec = Specification.where(null);

        if (role != null && !role.isBlank()) {
            spec = spec.and(UserSpecifications.hasRole(role));
        }
        if (enabled != null) {
            spec = spec.and(UserSpecifications.isEnabled(enabled));
        }
        if (accountLocked != null) {
            spec = spec.and(UserSpecifications.isAccountLocked(accountLocked));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(UserSpecifications.searchByKeyword(search));
        }

        return userRepository.findAll(spec, pageable).map(this::toAdminUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        log.debug("Admin fetching user detail for userId={}", userId);
        User user = findUserOrThrow(userId);
        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        log.info("Admin creating user: name='{}' email='{}' phone='{}'",
                request.getName(), request.getEmail(), request.getPhone());

        validateUniqueness(request);
        Set<Role> roles = resolveRoles(request.getRoles());

        String username = generateUsername(request.getName());

        User user = User.builder()
                .name(request.getName())
                .username(username)
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .emailVerified(request.getEmail() != null)
                .phoneVerified(request.getPhone() != null)
                .accountLocked(false)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("Admin created user userId={} username='{}'", user.getId(), username);

        // Track linked auth providers
        trackAuthProviders(user);

        // Publish event for messaging module (e.g., welcome email with temp password)
        Set<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
        eventPublisher.publishEvent(new AdminUserCreatedEvent(
                user.getId(), user.getName(), user.getEmail(), user.getPhone(), roleNames));

        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, AdminUpdateUserStatusRequest request) {
        log.info("Admin updating status for userId={} enabled={}", userId, request.getEnabled());

        User user = findUserOrThrow(userId);
        user.setEnabled(request.getEnabled());
        user = userRepository.save(user);

        log.info("User status updated: userId={} enabled={}", userId, request.getEnabled());
        return toAdminUserResponse(user);
    }

    // ── Update roles ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse updateUserRoles(Long userId, AdminUpdateRolesRequest request) {
        log.info("Admin updating roles for userId={} roles={}", userId, request.getRoles());

        User user = findUserOrThrow(userId);
        Set<Role> newRoles = resolveRoles(request.getRoles());
        user.setRoles(newRoles);
        user = userRepository.save(user);

        log.info("User roles updated: userId={} roles={}", userId, request.getRoles());
        return toAdminUserResponse(user);
    }

    // ── Unlock account ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse unlockUser(Long userId) {
        log.info("Admin unlocking user userId={}", userId);

        User user = findUserOrThrow(userId);
        user.setAccountLocked(false);
        user = userRepository.save(user);

        log.info("User unlocked: userId={}", userId);
        return toAdminUserResponse(user);
    }

    // ── Force password reset ─────────────────────────────────────────────

    @Override
    @Transactional
    public void forcePasswordReset(Long userId) {
        log.info("Admin forcing password reset for userId={}", userId);

        User user = findUserOrThrow(userId);

        // Generate a secure temporary password
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // The messaging module should deliver this temp password to the user.
        // For now we log it (Phase 1 — no real email/SMS delivery yet).
        log.info("Password reset forced for userId={}. Temp password generated.", userId);
    }

    // ── Login history ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> getUserLoginHistory(Long userId, Pageable pageable) {
        log.debug("Admin fetching login history for userId={}", userId);

        // Verify user exists before querying history
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }

        return loginHistoryRepository.findByUserIdOrderByLoggedInAtDesc(userId, pageable)
                .map(this::toLoginHistoryResponse);
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private void validateUniqueness(AdminCreateUserRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email '" + request.getEmail() + "' is already registered.");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException(
                    "Phone '" + request.getPhone() + "' is already registered.");
        }
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    /**
     * Auto-generates a username from the user's name by lower-casing and
     * stripping non-alphanumeric characters. Appends a numeric suffix if the
     * base username is already taken.
     */
    private String generateUsername(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    /**
     * Records {@link UserAuthProvider} entries for the newly created user,
     * based on which identifiers (email, phone) were provided.
     */
    private void trackAuthProviders(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()
                && !authProviderRepository.existsByUserIdAndProviderType(user.getId(), ProviderType.LOCAL_EMAIL)) {
            authProviderRepository.save(UserAuthProvider.builder()
                    .userId(user.getId())
                    .providerType(ProviderType.LOCAL_EMAIL)
                    .linkedAt(LocalDateTime.now())
                    .build());
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()
                && !authProviderRepository.existsByUserIdAndProviderType(user.getId(), ProviderType.LOCAL_PHONE)) {
            authProviderRepository.save(UserAuthProvider.builder()
                    .userId(user.getId())
                    .providerType(ProviderType.LOCAL_PHONE)
                    .linkedAt(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * Generates a random 12-character temporary password composed of
     * alphanumeric characters and a few special characters.
     */
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
        StringBuilder sb = new StringBuilder(12);
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .accountLocked(user.isAccountLocked())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private LoginHistoryResponse toLoginHistoryResponse(LoginHistory history) {
        return LoginHistoryResponse.builder()
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .deviceName(history.getDeviceName())
                .loggedInAt(history.getLoggedInAt())
                .build();
    }
}
