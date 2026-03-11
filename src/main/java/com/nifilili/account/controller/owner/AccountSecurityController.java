package com.nifilili.account.controller.owner;

import com.nifilili.account.dto.request.ChangePasswordRequest;
import com.nifilili.account.dto.request.RequestPasswordResetRequest;
import com.nifilili.account.dto.request.ResetPasswordByLinkRequest;
import com.nifilili.account.dto.request.ResetPasswordByOtpRequest;
import com.nifilili.account.dto.request.VerifyEmailRequest;
import com.nifilili.account.service.AccountSecurityService;
import com.nifilili.account.service.EmailVerificationService;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account/security")
@RequiredArgsConstructor
@Slf4j
@Tag(name = SwaggerConstants.ACCOUNT_1, description = "Password management, email verification, and account security operations.")
public class AccountSecurityController {

    private final AccountSecurityService accountSecurityService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    @Operation(summary = "Change Password", description = "Changes the authenticated user's password. Requires current password.")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Current password is incorrect or validation failed")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request");
        accountSecurityService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @Operation(summary = "Request Password Reset",
            description = "Initiates a password reset. Type LINK sends a tokenized email link (1h). "
                    + "Type OTP sends a 6-digit code (10min). Public endpoint — no auth required.")
    @ApiResponse(responseCode = "200", description = "Reset email sent (if account exists)")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody RequestPasswordResetRequest request) {
        log.info("Password reset request for type={}", request.getType());
        accountSecurityService.requestPasswordReset(request.getEmail(), request.getType());
        return ResponseEntity.ok(Map.of("message",
                "If an account exists with that email, a password reset has been sent."));
    }

    @Operation(summary = "Reset Password via Link",
            description = "Resets the password using a tokenized link from the email. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    @PostMapping("/reset-password-link")
    public ResponseEntity<Map<String, String>> resetPasswordByLink(
            @Valid @RequestBody ResetPasswordByLinkRequest request) {
        log.info("Password reset via link");
        accountSecurityService.resetPasswordByLink(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. Please login."));
    }

    @Operation(summary = "Reset Password via OTP",
            description = "Resets the password using a 6-digit OTP from the email. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping("/reset-password-otp")
    public ResponseEntity<Map<String, String>> resetPasswordByOtp(
            @Valid @RequestBody ResetPasswordByOtpRequest request) {
        log.info("Password reset via OTP");
        accountSecurityService.resetPasswordByOtp(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. Please login."));
    }

    @Operation(summary = "Verify Email",
            description = "Verifies the user's email address using the token from the verification email. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification request");
        emailVerificationService.verifyEmail(request.getToken());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    @Operation(summary = "Resend Verification Email",
            description = "Resends the verification email. Requires authentication.")
    @ApiResponse(responseCode = "200", description = "Verification email sent")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        log.info("Resend verification request for userId={}", userId);
        emailVerificationService.resendVerification(userId, user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification email sent."));
    }
}
