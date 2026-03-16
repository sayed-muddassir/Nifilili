package com.nifilili.auth.service.impl;

import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.AuthType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.domain.enums.ProviderType;
import com.nifilili.auth.dto.request.AuthRequest;
import com.nifilili.auth.dto.request.RegistrationRequest;
import com.nifilili.auth.dto.response.AuthResult;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.AuthProviderStrategy;
import com.nifilili.auth.service.OtpService;
import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.PhoneAlreadyExistsException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Handles phone number + OTP authentication and registration.
 * OTP must be pre-generated via {@code POST /api/auth/otp/request} before calling
 * authenticate or register.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PhoneOtpAuthProvider implements AuthProviderStrategy {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean supports(AuthType authType) {
        return AuthType.PHONE_OTP == authType;
    }

    @Override
    @Transactional
    public AuthResult authenticate(AuthRequest request, String ipAddress, String userAgent, String deviceName) {
        String phone = normalizePhone(request.getPhone());
        log.debug("Phone OTP auth for phone='{}'", phone);

        if (!otpService.verifyOtp(phone, request.getOtp(), OtpPurpose.LOGIN)) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found for this phone number. Please register first."));

        recordLoginHistory(user.getId(), ipAddress, userAgent, deviceName);
        log.info("Phone OTP login succeeded for userId={}", user.getId());

        return AuthResult.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    @Override
    @Transactional
    public AuthResult register(RegistrationRequest request, String ipAddress, String userAgent, String deviceName) {
        String phone = normalizePhone(request.getPhone());
        log.debug("Phone OTP registration for phone='{}'", phone);

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required for PHONE_OTP registration");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for registration");
        }

        // Verify the OTP before creating the account
        if (!otpService.verifyOtp(phone, request.getOtp(), OtpPurpose.SIGNUP)) {
            throw new InvalidTokenException("Invalid or expired OTP. Please request a new one.");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException("This phone number is already associated with an account");
        }

        // Check email uniqueness if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new PhoneAlreadyExistsException(
                    "This email is already associated with an account. Please login instead.");
        }

        String username = resolveUsername(request);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not seeded in database"));

        User newUser = User.builder()
                .name(request.getName())
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(phone)
                .enabled(true)
                .emailVerified(false)
                .phoneVerified(true) // Verified via OTP during registration
                .accountLocked(false)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(newUser);
        log.info("Registered user id={} via PHONE_OTP", saved.getId());

        // Track linked identity
        authProviderRepository.save(UserAuthProvider.builder()
                .userId(saved.getId())
                .providerType(ProviderType.LOCAL_PHONE)
                .linkedAt(LocalDateTime.now())
                .build());

        recordLoginHistory(saved.getId(), ipAddress, userAgent, deviceName);

        // Publish event — email may be null for phone-only registrations
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

    private String normalizePhone(String phone) {
//        if (phone == null) {
//            return null;
//        }
//        return phone.replaceAll("[\\s\\-]", "");
        return phone;
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
