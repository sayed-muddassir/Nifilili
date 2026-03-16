package com.nifilili.account.controller.admin;

import com.nifilili.account.dto.request.AdminCreateUserRequest;
import com.nifilili.account.dto.request.AdminUpdateRolesRequest;
import com.nifilili.account.dto.request.AdminUpdateUserStatusRequest;
import com.nifilili.account.dto.response.AdminUserResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.service.AdminUserService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = SwaggerConstants.ADMIN_USERS,
        description = "Administrative user management — create, list, enable/disable, "
                + "manage roles, unlock accounts, and force password resets.")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List users",
            description = "Returns a paginated list of users with optional filters for role, "
                    + "enabled status, account lock status, and keyword search.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> listUsers(
            @Parameter(description = "Filter by role name (e.g., ROLE_USER, ROLE_ADMIN)")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by enabled status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Filter by account locked status")
            @RequestParam(required = false) Boolean accountLocked,
            @Parameter(description = "Search by name, email, phone, or username")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("Admin list users request: role={}, enabled={}, locked={}, search='{}'",
                role, enabled, accountLocked, search);
        return ResponseEntity.ok(adminUserService.listUsers(role, enabled, accountLocked, search, pageable));
    }


    @Operation(summary = "Get user detail",
            description = "Retrieves detailed information for a specific user by their ID.")
    @ApiResponse(responseCode = "200", description = "User details retrieved")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long userId) {
        log.info("Admin get user request for userId={}", userId);
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }


    @Operation(summary = "Create user",
            description = "Creates a new user account with the specified roles and a temporary password. "
                    + "Requires ADMIN role. At least one of email or phone must be provided.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Email or phone already registered")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        log.info("Admin create user request: name='{}'", request.getName());
        AdminUserResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(summary = "Update user status",
            description = "Enables or disables a user account.")
    @ApiResponse(responseCode = "200", description = "User status updated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PutMapping("/{userId}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        log.info("Admin update status request for userId={}", userId);
        return ResponseEntity.ok(adminUserService.updateUserStatus(userId, request));
    }


    @Operation(summary = "Update user roles",
            description = "Replaces all roles for a user with the specified set of roles.")
    @ApiResponse(responseCode = "200", description = "Roles updated")
    @ApiResponse(responseCode = "404", description = "User or role not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PutMapping("/{userId}/roles")
    public ResponseEntity<AdminUserResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateRolesRequest request) {
        log.info("Admin update roles request for userId={}", userId);
        return ResponseEntity.ok(adminUserService.updateUserRoles(userId, request));
    }


    @Operation(summary = "Unlock user account",
            description = "Unlocks a locked user account, allowing them to log in again.")
    @ApiResponse(responseCode = "200", description = "Account unlocked")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<AdminUserResponse> unlockUser(@PathVariable Long userId) {
        log.info("Admin unlock request for userId={}", userId);
        return ResponseEntity.ok(adminUserService.unlockUser(userId));
    }


    @Operation(summary = "Force password reset",
            description = "Generates a new temporary password for the user and triggers a notification.")
    @ApiResponse(responseCode = "200", description = "Password reset initiated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PostMapping("/{userId}/force-password-reset")
    public ResponseEntity<Map<String, String>> forcePasswordReset(@PathVariable Long userId) {
        log.info("Admin force password reset for userId={}", userId);
        adminUserService.forcePasswordReset(userId);
        return ResponseEntity.ok(Map.of("message", "Password reset initiated. User will receive a new temporary password."));
    }

    // ── Login history ────────────────────────────────────────────────────

    @Operation(summary = "View user login history",
            description = "Returns a paginated list of login history entries for a specific user.")
    @ApiResponse(responseCode = "200", description = "Login history retrieved")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @GetMapping("/{userId}/login-history")
    public ResponseEntity<Page<LoginHistoryResponse>> getUserLoginHistory(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "loggedInAt") Pageable pageable) {
        log.info("Admin login history request for userId={}", userId);
        return ResponseEntity.ok(adminUserService.getUserLoginHistory(userId, pageable));
    }
}
