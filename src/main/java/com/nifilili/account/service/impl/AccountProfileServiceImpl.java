package com.nifilili.account.service.impl;

import com.nifilili.account.domain.UserProfile;
import com.nifilili.account.dto.request.UpdateProfileRequest;
import com.nifilili.account.dto.response.UserProfileDetailResponse;
import com.nifilili.account.mapper.UserProfileMapper;
import com.nifilili.account.repository.UserProfileRepository;
import com.nifilili.account.service.AccountProfileService;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountProfileServiceImpl implements AccountProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDetailResponse getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = findUser(userId);
        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);
        return buildResponse(user, profile);
    }

    @Override
    @Transactional
    public UserProfileDetailResponse updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = findUser(userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .userId(userId)
                            .build();
                    return newProfile;
                });

        // Apply partial update — MapStruct skips null fields
        profileMapper.updateFromRequest(request, profile);

        profileRepository.save(profile);
        log.info("Updated profile for userId={}", userId);

        return buildResponse(user, profile);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserProfileDetailResponse buildResponse(User user, UserProfile profile) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        UserProfileDetailResponse.UserProfileDetailResponseBuilder builder = UserProfileDetailResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(roleNames);

        if (profile != null) {
            builder.avatarUrl(profile.getAvatarUrl())
                    .bio(profile.getBio())
                    .dateOfBirth(profile.getDateOfBirth())
                    .gender(profile.getGender())
                    .streetAddress(profile.getStreetAddress())
                    .city(profile.getCity())
                    .state(profile.getState())
                    .country(profile.getCountry())
                    .postalCode(profile.getPostalCode());
        }

        return builder.build();
    }
}
