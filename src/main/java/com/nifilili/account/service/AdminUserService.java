package com.nifilili.account.service;

import com.nifilili.account.dto.request.AdminCreateUserRequest;
import com.nifilili.account.dto.request.AdminUpdateRolesRequest;
import com.nifilili.account.dto.request.AdminUpdateUserStatusRequest;
import com.nifilili.account.dto.response.AdminUserResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Administrative user management operations.
 * <p>
 * Provides capabilities for super admins and administrators to create, list,
 * enable/disable, manage roles, unlock, and force password resets on user accounts.
 */
public interface AdminUserService {

    /**
     * Lists users with optional filters and pagination.
     *
     * @param role          filter by role name (nullable)
     * @param enabled       filter by enabled status (nullable)
     * @param accountLocked filter by account locked status (nullable)
     * @param search        search by name, email, or phone (nullable)
     * @param pageable      pagination and sort parameters
     * @return paginated list of user summaries
     */
    Page<AdminUserResponse> listUsers(String role, Boolean enabled, Boolean accountLocked,
                                      String search, Pageable pageable);

    /**
     * Retrieves detailed information for a specific user.
     *
     * @param userId the target user's ID
     * @return detailed user response
     * @throws com.nifilili.core.exception.ResourceNotFoundException if user not found
     */
    AdminUserResponse getUserById(Long userId);

    /**
     * Creates a new user account with the specified roles.
     * Publishes an {@link com.nifilili.account.events.AdminUserCreatedEvent} upon success.
     *
     * @param request the user creation payload (name, email/phone, password, roles)
     * @return the created user's details
     * @throws com.nifilili.core.exception.EmailAlreadyExistsException if email is taken
     * @throws com.nifilili.core.exception.PhoneAlreadyExistsException if phone is taken
     * @throws com.nifilili.core.exception.ResourceNotFoundException   if a specified role does not exist
     */
    AdminUserResponse createUser(AdminCreateUserRequest request);

    /**
     * Enables or disables a user account.
     *
     * @param userId  the target user's ID
     * @param request contains the desired enabled status
     * @return the updated user details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if user not found
     */
    AdminUserResponse updateUserStatus(Long userId, AdminUpdateUserStatusRequest request);

    /**
     * Replaces all roles for a user with the specified set.
     *
     * @param userId  the target user's ID
     * @param request contains the new set of role names
     * @return the updated user details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if user or role not found
     */
    AdminUserResponse updateUserRoles(Long userId, AdminUpdateRolesRequest request);

    /**
     * Unlocks a locked user account and resets the failed login attempt counter.
     *
     * @param userId the target user's ID
     * @return the updated user details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if user not found
     */
    AdminUserResponse unlockUser(Long userId);

    /**
     * Forces a password reset for the user. Generates a new temporary password,
     * updates the user record, and triggers a notification.
     *
     * @param userId the target user's ID
     * @throws com.nifilili.core.exception.ResourceNotFoundException if user not found
     */
    void forcePasswordReset(Long userId);

    /**
     * Retrieves paginated login history for a specific user.
     *
     * @param userId   the target user's ID
     * @param pageable pagination and sort parameters
     * @return paginated login history entries
     * @throws com.nifilili.core.exception.ResourceNotFoundException if user not found
     */
    Page<LoginHistoryResponse> getUserLoginHistory(Long userId, Pageable pageable);
}
