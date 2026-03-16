package com.nifilili.account.controller.owner;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.account.dto.request.ChangePasswordRequest;
import com.nifilili.account.dto.request.RequestPasswordResetRequest;
import com.nifilili.account.dto.request.ResetPasswordByLinkRequest;
import com.nifilili.account.dto.request.ResetPasswordByOtpRequest;
import com.nifilili.account.dto.request.VerifyEmailRequest;
import com.nifilili.account.service.AccountSecurityService;
import com.nifilili.account.service.EmailVerificationService;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountSecurityControllerTest {

    @Mock
    private AccountSecurityService accountSecurityService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountSecurityController accountSecurityController;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(accountSecurityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextTestUtil.setAuthenticatedUser(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // changePassword
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void changePassword_WhenValidRequest_ShouldReturn200WithMessage() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "NewPass@456");

        mvc.perform(post("/api/v1/account/security/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(accountSecurityService).changePassword("OldPass@123", "NewPass@456");
    }

    @Test
    void changePassword_WhenCurrentPasswordBlank_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "NewPass@456");

        mvc.perform(post("/api/v1/account/security/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountSecurityService, never()).changePassword(anyString(), anyString());
    }

    @Test
    void changePassword_WhenNewPasswordTooWeak_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "weak");

        mvc.perform(post("/api/v1/account/security/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountSecurityService, never()).changePassword(anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // requestPasswordReset
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void requestPasswordReset_WhenValidLinkRequest_ShouldReturn200() throws Exception {
        RequestPasswordResetRequest request = new RequestPasswordResetRequest("user@test.com", "LINK");

        mvc.perform(post("/api/v1/account/security/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(accountSecurityService).requestPasswordReset("user@test.com", "LINK");
    }

    @Test
    void requestPasswordReset_WhenEmailBlank_ShouldReturn400() throws Exception {
        RequestPasswordResetRequest request = new RequestPasswordResetRequest("", "LINK");

        mvc.perform(post("/api/v1/account/security/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountSecurityService, never()).requestPasswordReset(anyString(), anyString());
    }

    @Test
    void requestPasswordReset_WhenInvalidType_ShouldReturn400() throws Exception {
        RequestPasswordResetRequest request = new RequestPasswordResetRequest("user@test.com", "INVALID");

        mvc.perform(post("/api/v1/account/security/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountSecurityService, never()).requestPasswordReset(anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resetPasswordByLink
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void resetPasswordByLink_WhenValidRequest_ShouldReturn200() throws Exception {
        ResetPasswordByLinkRequest request = new ResetPasswordByLinkRequest("valid-token", "NewPass@789");

        mvc.perform(post("/api/v1/account/security/reset-password-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully. Please login."));

        verify(accountSecurityService).resetPasswordByLink("valid-token", "NewPass@789");
    }

    @Test
    void resetPasswordByLink_WhenTokenBlank_ShouldReturn400() throws Exception {
        ResetPasswordByLinkRequest request = new ResetPasswordByLinkRequest("", "NewPass@789");

        mvc.perform(post("/api/v1/account/security/reset-password-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountSecurityService, never()).resetPasswordByLink(anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resetPasswordByOtp
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void resetPasswordByOtp_WhenValidRequest_ShouldReturn200() throws Exception {
        ResetPasswordByOtpRequest request = new ResetPasswordByOtpRequest("user@test.com", "482917", "NewPass@789");

        mvc.perform(post("/api/v1/account/security/reset-password-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully. Please login."));

        verify(accountSecurityService).resetPasswordByOtp("user@test.com", "482917", "NewPass@789");
    }

    @Test
    void resetPasswordByOtp_WhenOtpWrongLength_ShouldReturn400() throws Exception {
        ResetPasswordByOtpRequest request = new ResetPasswordByOtpRequest("user@test.com", "123", "NewPass@789");

        mvc.perform(post("/api/v1/account/security/reset-password-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountSecurityService, never()).resetPasswordByOtp(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyEmail
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void verifyEmail_WhenValidToken_ShouldReturn200() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("valid-verification-token");

        mvc.perform(post("/api/v1/account/security/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully."));

        verify(emailVerificationService).verifyEmail("valid-verification-token");
    }

    @Test
    void verifyEmail_WhenTokenBlank_ShouldReturn400() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("");

        mvc.perform(post("/api/v1/account/security/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailVerificationService, never()).verifyEmail(anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resendVerification
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void resendVerification_WhenUserExists_ShouldReturn200() throws Exception {
        User user = User.builder().email("test@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(post("/api/v1/account/security/resend-verification"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification email sent."));

        verify(emailVerificationService).resendVerification(1L, "test@example.com");
    }

    @Test
    void resendVerification_WhenUserNotFound_ShouldReturn404() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/v1/account/security/resend-verification"))
                .andExpect(status().isNotFound());

        verify(emailVerificationService, never()).resendVerification(anyLong(), anyString());
    }
}
