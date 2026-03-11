package com.nifilili.account.controller.owner;

import com.nifilili.account.dto.request.UpdateProfileRequest;
import com.nifilili.account.dto.response.UserProfileDetailResponse;
import com.nifilili.account.service.AccountProfileService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = SwaggerConstants.ACCOUNT_2, description = "User profile management (avatar, bio, address, etc.).")
public class AccountProfileController {

    private final AccountProfileService accountProfileService;

    @Operation(summary = "Get Full Profile",
            description = "Returns the authenticated user's full profile including auth details and extended profile.")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping
    public ResponseEntity<UserProfileDetailResponse> getProfile() {
        log.debug("Get profile request");
        return ResponseEntity.ok(accountProfileService.getProfile());
    }

    @Operation(summary = "Update Profile",
            description = "Updates the authenticated user's extended profile. Only non-null fields are updated.")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PutMapping
    public ResponseEntity<UserProfileDetailResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile request");
        return ResponseEntity.ok(accountProfileService.updateProfile(request));
    }
}
