package com.nifilili.account.controller.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.account.dto.request.AdminCreateUserRequest;
import com.nifilili.account.dto.request.AdminUpdateRolesRequest;
import com.nifilili.account.dto.request.AdminUpdateUserStatusRequest;
import com.nifilili.account.dto.response.AdminUserResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.service.AdminUserService;
import com.nifilili.core.exception.GlobalExceptionHandler;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AdminUserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(adminUserController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = AdminUserResponse.builder()
                .id(101L)
                .name("Jane Doe")
                .username("janedoe")
                .email("jane.doe@nifilili.com")
                .phone("+977-9800000000")
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(false)
                .accountLocked(false)
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.of(2026, 3, 10, 14, 30))
                .updatedAt(LocalDateTime.of(2026, 3, 10, 14, 30))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listUsers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void listUsers_WhenNoFilters_ShouldReturn200WithPage() throws Exception {
        Page<AdminUserResponse> page = new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(adminUserService.listUsers(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$.content[0].email").value("jane.doe@nifilili.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listUsers_WhenFilterByRole_ShouldPassFilterToService() throws Exception {
        Page<AdminUserResponse> page = new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(adminUserService.listUsers(eq("ROLE_ADMIN"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/api/v1/admin/users").param("role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jane Doe"));

        verify(adminUserService).listUsers(eq("ROLE_ADMIN"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getUserById_WhenUserExists_ShouldReturn200() throws Exception {
        when(adminUserService.getUserById(101L)).thenReturn(sampleResponse);

        mvc.perform(get("/api/v1/admin/users/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@nifilili.com"));

        verify(adminUserService).getUserById(101L);
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldReturn404() throws Exception {
        when(adminUserService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User not found"));

        mvc.perform(get("/api/v1/admin/users/999"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createUser_WhenValidRequest_ShouldReturn201() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Jane Doe", "jane@nifilili.com", "+977-9800000000", "TempPass@123", Set.of("ROLE_USER"));
        when(adminUserService.createUser(any(AdminCreateUserRequest.class))).thenReturn(sampleResponse);

        mvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(adminUserService).createUser(any(AdminCreateUserRequest.class));
    }

    @Test
    void createUser_WhenNameBlank_ShouldReturn400() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "", "jane@nifilili.com", null, "TempPass@123", Set.of("ROLE_USER"));

        mvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).createUser(any());
    }

    @Test
    void createUser_WhenPasswordTooShort_ShouldReturn400() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Jane Doe", "jane@nifilili.com", null, "short", Set.of("ROLE_USER"));

        mvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).createUser(any());
    }

    @Test
    void createUser_WhenRolesEmpty_ShouldReturn400() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Jane Doe", "jane@nifilili.com", null, "TempPass@123", Set.of());

        mvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).createUser(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateUserStatus
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void updateUserStatus_WhenValidRequest_ShouldReturn200() throws Exception {
        AdminUpdateUserStatusRequest request = new AdminUpdateUserStatusRequest(false);
        AdminUserResponse updatedResponse = AdminUserResponse.builder()
                .id(101L).name("Jane Doe").enabled(false).build();
        when(adminUserService.updateUserStatus(eq(101L), any(AdminUpdateUserStatusRequest.class)))
                .thenReturn(updatedResponse);

        mvc.perform(put("/api/v1/admin/users/101/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        verify(adminUserService).updateUserStatus(eq(101L), any(AdminUpdateUserStatusRequest.class));
    }

    @Test
    void updateUserStatus_WhenEnabledNull_ShouldReturn400() throws Exception {
        // Send JSON with null enabled field
        String json = "{\"enabled\": null}";

        mvc.perform(put("/api/v1/admin/users/101/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).updateUserStatus(anyLong(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateUserRoles
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void updateUserRoles_WhenValidRequest_ShouldReturn200() throws Exception {
        AdminUpdateRolesRequest request = new AdminUpdateRolesRequest(Set.of("ROLE_USER", "ROLE_ADMIN"));
        AdminUserResponse updatedResponse = AdminUserResponse.builder()
                .id(101L).name("Jane Doe").roles(Set.of("ROLE_USER", "ROLE_ADMIN")).build();
        when(adminUserService.updateUserRoles(eq(101L), any(AdminUpdateRolesRequest.class)))
                .thenReturn(updatedResponse);

        mvc.perform(put("/api/v1/admin/users/101/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101));

        verify(adminUserService).updateUserRoles(eq(101L), any(AdminUpdateRolesRequest.class));
    }

    @Test
    void updateUserRoles_WhenRolesEmpty_ShouldReturn400() throws Exception {
        AdminUpdateRolesRequest request = new AdminUpdateRolesRequest(Set.of());

        mvc.perform(put("/api/v1/admin/users/101/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).updateUserRoles(anyLong(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // unlockUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void unlockUser_WhenUserExists_ShouldReturn200() throws Exception {
        AdminUserResponse unlockedResponse = AdminUserResponse.builder()
                .id(101L).name("Jane Doe").accountLocked(false).build();
        when(adminUserService.unlockUser(101L)).thenReturn(unlockedResponse);

        mvc.perform(post("/api/v1/admin/users/101/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountLocked").value(false));

        verify(adminUserService).unlockUser(101L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // forcePasswordReset
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void forcePasswordReset_WhenUserExists_ShouldReturn200WithMessage() throws Exception {
        doNothing().when(adminUserService).forcePasswordReset(101L);

        mvc.perform(post("/api/v1/admin/users/101/force-password-reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(adminUserService).forcePasswordReset(101L);
    }

    @Test
    void forcePasswordReset_WhenUserNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(adminUserService).forcePasswordReset(999L);

        mvc.perform(post("/api/v1/admin/users/999/force-password-reset"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserLoginHistory
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getUserLoginHistory_WhenUserExists_ShouldReturn200WithPage() throws Exception {
        LoginHistoryResponse historyEntry = LoginHistoryResponse.builder()
                .ipAddress("192.168.1.1")
                .userAgent("Chrome/120")
                .deviceName("Chrome on MacOS")
                .loggedInAt(LocalDateTime.of(2026, 3, 10, 14, 30))
                .build();
        Page<LoginHistoryResponse> page = new PageImpl<>(List.of(historyEntry), PageRequest.of(0, 20), 1);
        when(adminUserService.getUserLoginHistory(eq(101L), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/v1/admin/users/101/login-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ipAddress").value("192.168.1.1"))
                .andExpect(jsonPath("$.content[0].deviceName").value("Chrome on MacOS"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
