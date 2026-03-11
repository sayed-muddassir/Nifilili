package com.nifilili.account.service.impl;

import com.nifilili.account.domain.UserProfile;
import com.nifilili.account.dto.request.UpdateProfileRequest;
import com.nifilili.account.dto.response.UserProfileDetailResponse;
import com.nifilili.account.mapper.UserProfileMapper;
import com.nifilili.account.repository.UserProfileRepository;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository profileRepository;
    @Mock private UserProfileMapper profileMapper;

    @InjectMocks private AccountProfileServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void getProfile_WhenProfileExists_ShouldReturnFullResponse() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John Doe").username("john").email("john@test.com")
                .phone("+977-9800000000").enabled(true).emailVerified(true)
                .roles(Set.of(role)).build();
        UserProfile profile = UserProfile.builder()
                .userId(1L).bio("Developer").city("Kathmandu").country("Nepal").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        UserProfileDetailResponse response = service.getProfile();

        assertEquals("john", response.getUsername());
        assertEquals("john@test.com", response.getEmail());
        assertTrue(response.isEmailVerified());
        assertEquals("Developer", response.getBio());
        assertEquals("Kathmandu", response.getCity());
        assertEquals("Nepal", response.getCountry());
    }

    @Test
    void getProfile_WhenNoExtendedProfile_ShouldReturnResponseWithoutProfileFields() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John Doe").username("john").email("john@test.com")
                .enabled(true).emailVerified(false).roles(Set.of(role)).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        UserProfileDetailResponse response = service.getProfile();

        assertEquals("john", response.getUsername());
        assertFalse(response.isEmailVerified());
        assertNull(response.getBio());
        assertNull(response.getCity());
    }

    @Test
    void getProfile_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        SecurityContextTestUtil.setAuthenticatedUser(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getProfile());
    }

    @Test
    void updateProfile_WhenProfileExists_ShouldDelegateToMapperAndSave() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John").username("john").email("john@test.com")
                .enabled(true).emailVerified(true).roles(Set.of(role)).build();
        UserProfile existing = UserProfile.builder()
                .userId(1L).bio("Old bio").city("OldCity").build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("New bio");
        request.setCity("Kathmandu");

        // Simulate MapStruct behavior: update the target profile when mapper is called
        doAnswer(inv -> {
            UpdateProfileRequest req = inv.getArgument(0);
            UserProfile target = inv.getArgument(1);
            if (req.getBio() != null) target.setBio(req.getBio());
            if (req.getCity() != null) target.setCity(req.getCity());
            return null;
        }).when(profileMapper).updateFromRequest(any(UpdateProfileRequest.class), any(UserProfile.class));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDetailResponse response = service.updateProfile(request);

        assertEquals("New bio", response.getBio());
        assertEquals("Kathmandu", response.getCity());
        verify(profileMapper).updateFromRequest(request, existing);
        verify(profileRepository).save(existing);
    }

    @Test
    void updateProfile_WhenNoProfileExists_ShouldCreateNewProfileAndDelegateToMapper() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John").username("john").email("john@test.com")
                .enabled(true).emailVerified(true).roles(Set.of(role)).build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("First bio");
        request.setDateOfBirth(LocalDate.of(1995, 6, 15));

        doAnswer(inv -> {
            UpdateProfileRequest req = inv.getArgument(0);
            UserProfile target = inv.getArgument(1);
            if (req.getBio() != null) target.setBio(req.getBio());
            if (req.getDateOfBirth() != null) target.setDateOfBirth(req.getDateOfBirth());
            return null;
        }).when(profileMapper).updateFromRequest(any(UpdateProfileRequest.class), any(UserProfile.class));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDetailResponse response = service.updateProfile(request);

        assertEquals("First bio", response.getBio());
        assertEquals(LocalDate.of(1995, 6, 15), response.getDateOfBirth());
        verify(profileMapper).updateFromRequest(eq(request), any(UserProfile.class));
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_WhenNullFieldsInRequest_ShouldPreserveExistingViaMapper() {
        SecurityContextTestUtil.setAuthenticatedUser(1L);
        Role role = new Role(2L, "ROLE_USER");
        User user = User.builder()
                .name("John").username("john").email("john@test.com")
                .enabled(true).emailVerified(true).roles(Set.of(role)).build();
        UserProfile existing = UserProfile.builder()
                .userId(1L).bio("Keep this").city("KeepCity").country("KeepCountry").build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setCity("NewCity");
        // bio and country are null — mapper skips null fields per @BeanMapping config

        doAnswer(inv -> {
            UpdateProfileRequest req = inv.getArgument(0);
            UserProfile target = inv.getArgument(1);
            // Simulate MapStruct IGNORE null strategy — only non-null fields updated
            if (req.getCity() != null) target.setCity(req.getCity());
            // bio is null, so it stays as-is
            // country is null, so it stays as-is
            return null;
        }).when(profileMapper).updateFromRequest(any(UpdateProfileRequest.class), any(UserProfile.class));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDetailResponse response = service.updateProfile(request);

        assertEquals("Keep this", response.getBio());
        assertEquals("NewCity", response.getCity());
        assertEquals("KeepCountry", response.getCountry());
    }
}
