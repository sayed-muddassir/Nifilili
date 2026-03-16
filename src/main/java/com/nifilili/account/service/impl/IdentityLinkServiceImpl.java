package com.nifilili.account.service.impl;

import com.nifilili.account.service.IdentityLinkService;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.domain.enums.ProviderType;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.auth.service.OtpService;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.InvalidTokenException;
import com.nifilili.core.exception.PhoneAlreadyExistsException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityLinkServiceImpl implements IdentityLinkService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final OtpService otpService;

    @Override
    @Transactional
    public void initiateLinkPhone(String phone) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Initiating phone link for userId={} phone='{}'", userId, phone);

        if (userRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException(
                    "This phone number is already associated with an account. Please login instead.");
        }

        otpService.generateAndSendOtp(phone, IdentifierType.PHONE, OtpPurpose.VERIFY_PHONE);
        log.info("Phone link OTP sent for userId={}", userId);
    }

    @Override
    @Transactional
    public void completeLinkPhone(String phone, String otp) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Completing phone link for userId={} phone='{}'", userId, phone);

        if (!otpService.verifyOtp(phone, otp, OtpPurpose.VERIFY_PHONE)) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setPhone(phone);
        user.setPhoneVerified(true);
        userRepository.save(user);

        // Track linked identity (if not already present)
        if (!authProviderRepository.existsByUserIdAndProviderType(userId, ProviderType.LOCAL_PHONE)) {
            authProviderRepository.save(UserAuthProvider.builder()
                    .userId(userId)
                    .providerType(ProviderType.LOCAL_PHONE)
                    .linkedAt(LocalDateTime.now())
                    .build());
        }

        log.info("Phone linked successfully for userId={}", userId);
    }

    @Override
    @Transactional
    public void initiateLinkEmail(String email) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Initiating email link for userId={} email='{}'", userId, email);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "This email is already associated with an account. Please login instead.");
        }

        otpService.generateAndSendOtp(email, IdentifierType.EMAIL, OtpPurpose.VERIFY_EMAIL);
        log.info("Email link OTP sent for userId={}", userId);
    }

    @Override
    @Transactional
    public void completeLinkEmail(String email, String otp) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Completing email link for userId={} email='{}'", userId, email);

        if (!otpService.verifyOtp(email, otp, OtpPurpose.VERIFY_EMAIL)) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setEmail(email);
        user.setEmailVerified(true);
        userRepository.save(user);

        if (!authProviderRepository.existsByUserIdAndProviderType(userId, ProviderType.LOCAL_EMAIL)) {
            authProviderRepository.save(UserAuthProvider.builder()
                    .userId(userId)
                    .providerType(ProviderType.LOCAL_EMAIL)
                    .linkedAt(LocalDateTime.now())
                    .build());
        }

        log.info("Email linked successfully for userId={}", userId);
    }
}
