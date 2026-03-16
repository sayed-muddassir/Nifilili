package com.nifilili.account.controller.owner;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nifilili.account.dto.request.UpdateProfileRequest;
import com.nifilili.account.dto.response.UserProfileDetailResponse;
import com.nifilili.account.service.AccountProfileService;
import com.nifilili.core.exception.GlobalExceptionHandler;
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
class AccountProfileControllerTest {

    @Mock
    private AccountProfileService accountProfileService;

    @InjectMocks
    private AccountProfileController accountProfileController;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserProfileDetailResponse sampleProfile;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(accountProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleProfile = UserProfileDetailResponse.builder()
                .id(1L)
                .name("John Doe")
                .username("johndoe")
                .email("john@nifilili.com")
                .phone("+977-9800000000")
                .enabled(true)
                .emailVerified(true)
                .roles(Set.of("ROLE_USER"))
                .avatarUrl("https://cdn.nifilili.com/avatars/user1.jpg")
                .bio("Full-stack developer")
                .dateOfBirth(LocalDate.of(1995, 6, 15))
                .gender("Male")
                .city("Kathmandu")
                .state("Bagmati")
                .country("Nepal")
                .postalCode("44600")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getProfile
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getProfile_WhenAuthenticated_ShouldReturn200WithProfile() throws Exception {
        when(accountProfileService.getProfile()).thenReturn(sampleProfile);

        mvc.perform(get("/api/v1/account/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@nifilili.com"))
                .andExpect(jsonPath("$.bio").value("Full-stack developer"))
                .andExpect(jsonPath("$.city").value("Kathmandu"));

        verify(accountProfileService).getProfile();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateProfile
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void updateProfile_WhenValidRequest_ShouldReturn200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("Updated bio");
        request.setCity("Pokhara");
        request.setDateOfBirth(LocalDate.of(1995, 6, 15));

        UserProfileDetailResponse updatedProfile = UserProfileDetailResponse.builder()
                .id(1L).name("John Doe").bio("Updated bio").city("Pokhara")
                .dateOfBirth(LocalDate.of(1995, 6, 15)).build();
        when(accountProfileService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(updatedProfile);

        mvc.perform(put("/api/v1/account/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.city").value("Pokhara"));

        verify(accountProfileService).updateProfile(any(UpdateProfileRequest.class));
    }

    @Test
    void updateProfile_WhenBioTooLong_ShouldReturn400() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("x".repeat(2001));

        mvc.perform(put("/api/v1/account/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountProfileService, never()).updateProfile(any());
    }

    @Test
    void updateProfile_WhenAllFieldsNull_ShouldReturn200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        when(accountProfileService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(sampleProfile);

        mvc.perform(put("/api/v1/account/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountProfileService).updateProfile(any(UpdateProfileRequest.class));
    }
}
