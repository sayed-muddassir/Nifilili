package com.nifilili.account.service;

import com.nifilili.account.dto.request.UpdateProfileRequest;
import com.nifilili.account.dto.response.UserProfileDetailResponse;

/**
 * Manages the authenticated user's extended profile (avatar, bio, address, etc.).
 */
public interface AccountProfileService {

    /**
     * Gets the full profile (auth user + extended profile) for the current user.
     *
     * @return full profile response
     */
    UserProfileDetailResponse getProfile();

    /**
     * Updates the extended profile for the current user. Creates the profile row if it doesn't exist.
     *
     * @param request the profile fields to update
     * @return updated full profile response
     */
    UserProfileDetailResponse updateProfile(UpdateProfileRequest request);
}
