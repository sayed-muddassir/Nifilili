package com.nifilili.auth.service.impl;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.config.JwtTokenProvider;
import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.RefreshToken;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.AuthService;
import com.nifilili.auth.service.LoginAttemptService;
import com.nifilili.auth.service.TokenService;
import com.nifilili.account.events.UserRegisteredEvent;
import com.nifilili.core.exception.AccountLockedException;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.UsernameAlreadyExistsException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public JwtAuthResponse login(LoginDto loginDto, String ipAddress, String userAgent, String deviceName) {
        String usernameOrEmail = loginDto.getUsernameOrEmail();
        log.debug("Authenticating principal '{}'", usernameOrEmail);

        // Check lockout before attempting authentication
        if (loginAttemptService.isAccountLocked(usernameOrEmail)) {
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Please reset your password.");
        }

        // Resolve userId for attempt tracking
        Long userId = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(User::getId)
                .orElse(null);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, loginDto.getPassword())
            );
        } catch (BadCredentialsException ex) {
            loginAttemptService.recordAttempt(userId, usernameOrEmail, ipAddress, false);
            throw ex;
        }

        // Record successful attempt
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        loginAttemptService.recordAttempt(principal.getUserId(), usernameOrEmail, ipAddress, true);

        // Record login history
        recordLoginHistory(principal.getUserId(), ipAddress, userAgent, deviceName);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate dual tokens
        String accessToken = jwtTokenProvider.generateAccessToken(principal.getUserId(), principal.getUsername());
        RefreshToken refreshToken = tokenService.createRefreshToken(
                principal.getUserId(), deviceName, ipAddress, userAgent);

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in repository"));

        log.info("User '{}' logged in successfully", user.getUsername());

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken.getToken())
                .user(buildProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public JwtAuthResponse register(RegisterDto registerDto, String ipAddress, String userAgent, String deviceName) {
        log.debug("Registering new user with email '{}'", registerDto.getEmail());

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email address is already registered: " + registerDto.getEmail());
        }
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username is already taken: " + registerDto.getUsername());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not seeded in database"));

        User newUser = User.builder()
                .name(registerDto.getName())
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .phone(registerDto.getPhone())
                .enabled(true)
                .emailVerified(false)
                .accountLocked(false)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(newUser);
        log.info("Registered new user id='{}' username='{}'", saved.getId(), saved.getUsername());

        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerDto.getUsername(), registerDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Generate dual tokens
        String accessToken = jwtTokenProvider.generateAccessToken(principal.getUserId(), principal.getUsername());
        RefreshToken refreshToken = tokenService.createRefreshToken(
                principal.getUserId(), deviceName, ipAddress, userAgent);

        // Record login history
        recordLoginHistory(principal.getUserId(), ipAddress, userAgent, deviceName);

        // Publish event for email verification
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail()));

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken.getToken())
                .user(buildProfileResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public JwtAuthResponse refresh(String refreshTokenStr) {
        RefreshToken newRefreshToken = tokenService.rotateRefreshToken(refreshTokenStr);

        User user = userRepository.findById(newRefreshToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for refresh token"));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());

        log.debug("Refreshed access token for userId={}", user.getId());

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(newRefreshToken.getToken())
                .user(buildProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void logoutAll() {
        Long userId = SecurityUtil.getCurrentUserId();
        tokenService.revokeAllRefreshTokens(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in repository"));
        return buildProfileResponse(user);
    }

    private void recordLoginHistory(Long userId, String ipAddress, String userAgent, String deviceName) {
        LoginHistory history = LoginHistory.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceName(deviceName)
                .loggedInAt(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(history);
    }

    private UserProfileResponse buildProfileResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(roleNames)
                .build();
    }
}
