package com.nifilili.account.controller.owner;

import com.nifilili.account.dto.request.LinkEmailRequest;
import com.nifilili.account.dto.request.LinkPhoneRequest;
import com.nifilili.account.dto.request.VerifyLinkRequest;
import com.nifilili.account.service.IdentityLinkService;
import com.nifilili.core.constants.SwaggerConstants;
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
@RequestMapping("/api/v1/account/identity")
@RequiredArgsConstructor
@Slf4j
@Tag(name = SwaggerConstants.ACCOUNT_4, description = "Link additional auth methods (phone, email) to account")
public class AccountIdentityController {

    private final IdentityLinkService identityLinkService;

    @Operation(summary = "Initiate phone linking", description = "Sends OTP to phone number for verification.")
    @ApiResponse(responseCode = "200", description = "OTP sent to phone")
    @ApiResponse(responseCode = "409", description = "Phone already registered")
    @ApiResponse(responseCode = "429", description = "OTP rate limit exceeded")
    @PostMapping("/link/phone")
    public ResponseEntity<Map<String, String>> linkPhone(@Valid @RequestBody LinkPhoneRequest request) {
        log.info("Phone link initiation request");
        identityLinkService.initiateLinkPhone(request.getPhone());
        return ResponseEntity.ok(Map.of("message", "OTP sent to phone number"));
    }

    @Operation(summary = "Complete phone linking", description = "Verifies OTP and links phone to account.")
    @ApiResponse(responseCode = "200", description = "Phone linked successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping("/link/phone/verify")
    public ResponseEntity<Map<String, String>> verifyLinkPhone(@Valid @RequestBody VerifyLinkRequest request) {
        log.info("Phone link verification request");
        identityLinkService.completeLinkPhone(request.getIdentifier(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Phone number linked successfully"));
    }

    @Operation(summary = "Initiate email linking", description = "Sends OTP to email address for verification.")
    @ApiResponse(responseCode = "200", description = "OTP sent to email")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @ApiResponse(responseCode = "429", description = "OTP rate limit exceeded")
    @PostMapping("/link/email")
    public ResponseEntity<Map<String, String>> linkEmail(@Valid @RequestBody LinkEmailRequest request) {
        log.info("Email link initiation request");
        identityLinkService.initiateLinkEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent to email address"));
    }

    @Operation(summary = "Complete email linking", description = "Verifies OTP and links email to account.")
    @ApiResponse(responseCode = "200", description = "Email linked successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping("/link/email/verify")
    public ResponseEntity<Map<String, String>> verifyLinkEmail(@Valid @RequestBody VerifyLinkRequest request) {
        log.info("Email link verification request");
        identityLinkService.completeLinkEmail(request.getIdentifier(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Email address linked successfully"));
    }
}
