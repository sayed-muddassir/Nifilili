package com.nifilili.account.service.impl;

import com.nifilili.account.dto.request.AdminCreateUserRequest;
import com.nifilili.account.dto.request.AdminUpdateRolesRequest;
import com.nifilili.account.dto.request.AdminUpdateUserStatusRequest;
import com.nifilili.account.dto.response.AdminUserResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.events.AdminUserCreatedEvent;
import com.nifilili.auth.domain.LoginHistory;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.domain.enums.ProviderType;
import com.nifilili.auth.repository.LoginHistoryRepository;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserAuthProviderRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.core.exception.EmailAlreadyExistsException;
import com.nifilili.core.exception.PhoneAlreadyExistsException;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private UserAuthProviderRepository authProviderRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<AdminUserCreatedEvent> eventCaptor;

    @InjectMocks private AdminUserServiceImpl adminUserService;

    // ── listUsers ────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void listUsers_WhenUsersExist_ShouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        User user = buildTestUser(1L, "Jane Doe", "janedoe", "jane@example.com", "+977-9800000000");

        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<AdminUserResponse> result = adminUserService.listUsers(null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        AdminUserResponse response = result.getContent().get(0);
        assertEquals("Jane Doe", response.getName());
        assertEquals("janedoe", response.getUsername());
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listUsers_WhenNoUsersMatch_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<AdminUserResponse> result = adminUserService.listUsers("ROLE_ADMIN", true, false, "nobody", pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ── getUserById ──────────────────────────────────────────────────────

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDetail() {
        User user = buildTestUser(1L, "Jane Doe", "janedoe", "jane@example.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserResponse response = adminUserService.getUserById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_USER"));
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserById(999L));
    }

    // ── createUser ───────────────────────────────────────────────────────

    @Test
    void createUser_WhenValidRequest_ShouldCreateAndPublishEvent() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("New User");
        request.setEmail("new@example.com");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_USER"));

        Role roleUser = new Role(1L, "ROLE_USER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("TempPass@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });
        when(authProviderRepository.existsByUserIdAndProviderType(100L, ProviderType.LOCAL_EMAIL)).thenReturn(false);

        AdminUserResponse response = adminUserService.createUser(request);

        assertNotNull(response);
        assertEquals("New User", response.getName());

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("New User", savedUser.getName());
        assertEquals("encoded-password", savedUser.getPassword());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.isEmailVerified());

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AdminUserCreatedEvent event = eventCaptor.getValue();
        assertEquals("New User", event.name());
        assertEquals("new@example.com", event.email());
        assertTrue(event.roles().contains("ROLE_USER"));
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowEmailAlreadyExistsException() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("Duplicate");
        request.setEmail("existing@example.com");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_USER"));

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> adminUserService.createUser(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenPhoneAlreadyExists_ShouldThrowPhoneAlreadyExistsException() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("Duplicate Phone");
        request.setPhone("+977-9800000000");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_USER"));

        when(userRepository.existsByPhone("+977-9800000000")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class, () -> adminUserService.createUser(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenRoleNotFound_ShouldThrowResourceNotFoundException() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("Bad Role");
        request.setEmail("bad@example.com");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_NONEXISTENT"));

        when(userRepository.existsByEmail("bad@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.createUser(request));
    }

    // ── updateUserStatus ─────────────────────────────────────────────────

    @Test
    void updateUserStatus_WhenUserExists_ShouldUpdateEnabled() {
        User user = buildTestUser(1L, "Jane", "jane", "jane@example.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUpdateUserStatusRequest request = new AdminUpdateUserStatusRequest(false);
        AdminUserResponse response = adminUserService.updateUserStatus(1L, request);

        assertFalse(response.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserStatus_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminUserService.updateUserStatus(999L, new AdminUpdateUserStatusRequest(false)));
    }

    // ── updateUserRoles ──────────────────────────────────────────────────

    @Test
    void updateUserRoles_WhenValidRoles_ShouldReplaceRoles() {
        User user = buildTestUser(1L, "Jane", "jane", "jane@example.com", null);
        Role roleAdmin = new Role(2L, "ROLE_ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUpdateRolesRequest request = new AdminUpdateRolesRequest(Set.of("ROLE_ADMIN"));
        AdminUserResponse response = adminUserService.updateUserRoles(1L, request);

        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
        assertFalse(response.getRoles().contains("ROLE_USER"));
    }

    @Test
    void updateUserRoles_WhenRoleNotFound_ShouldThrowResourceNotFoundException() {
        User user = buildTestUser(1L, "Jane", "jane", "jane@example.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminUserService.updateUserRoles(1L, new AdminUpdateRolesRequest(Set.of("ROLE_NONEXISTENT"))));
    }

    // ── unlockUser ───────────────────────────────────────────────────────

    @Test
    void unlockUser_WhenUserIsLocked_ShouldUnlockAccount() {
        User user = buildTestUser(1L, "Jane", "jane", "jane@example.com", null);
        user.setAccountLocked(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse response = adminUserService.unlockUser(1L);

        assertFalse(response.isAccountLocked());
        verify(userRepository).save(user);
    }

    @Test
    void unlockUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.unlockUser(999L));
    }

    // ── forcePasswordReset ───────────────────────────────────────────────

    @Test
    void forcePasswordReset_WhenUserExists_ShouldEncodeNewPassword() {
        User user = buildTestUser(1L, "Jane", "jane", "jane@example.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminUserService.forcePasswordReset(1L);

        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded-temp-password", userCaptor.getValue().getPassword());
    }

    @Test
    void forcePasswordReset_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.forcePasswordReset(999L));
    }

    // ── getUserLoginHistory ──────────────────────────────────────────────

    @Test
    void getUserLoginHistory_WhenHistoryExists_ShouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now();

        LoginHistory entry = LoginHistory.builder()
                .userId(1L).ipAddress("192.168.1.1").userAgent("Mozilla/5.0")
                .deviceName("Chrome").loggedInAt(now).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(loginHistoryRepository.findByUserIdOrderByLoggedInAtDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));

        Page<LoginHistoryResponse> result = adminUserService.getUserLoginHistory(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("192.168.1.1", result.getContent().get(0).getIpAddress());
    }

    @Test
    void getUserLoginHistory_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> adminUserService.getUserLoginHistory(999L, pageable));
    }

    // ── createUser — username generation ────────────────────────────────

    @Test
    void createUser_WhenUsernameConflict_ShouldAppendNumericSuffix() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("Jane Doe");
        request.setEmail("jane2@example.com");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_USER"));

        Role roleUser = new Role(1L, "ROLE_USER");

        when(userRepository.existsByEmail("jane2@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);
        when(userRepository.existsByUsername("janedoe1")).thenReturn(true);
        when(userRepository.existsByUsername("janedoe2")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("TempPass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(200L);
            return u;
        });
        when(authProviderRepository.existsByUserIdAndProviderType(200L, ProviderType.LOCAL_EMAIL)).thenReturn(false);

        AdminUserResponse response = adminUserService.createUser(request);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("janedoe2", userCaptor.getValue().getUsername());
    }

    @Test
    void createUser_WhenOnlyPhoneProvided_ShouldSetPhoneVerifiedAndTrackPhoneProvider() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("Phone User");
        request.setPhone("+977-9800000001");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_USER"));

        Role roleUser = new Role(1L, "ROLE_USER");

        when(userRepository.existsByPhone("+977-9800000001")).thenReturn(false);
        when(userRepository.existsByUsername("phoneuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("TempPass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(300L);
            return u;
        });
        when(authProviderRepository.existsByUserIdAndProviderType(300L, ProviderType.LOCAL_PHONE)).thenReturn(false);

        AdminUserResponse response = adminUserService.createUser(request);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertFalse(saved.isEmailVerified());
        assertTrue(saved.isPhoneVerified());
        verify(authProviderRepository).save(any());
    }

    @Test
    void createUser_WhenNameHasSpecialChars_ShouldNormalizeUsername() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setName("J@ne D'oe-Smith!");
        request.setEmail("jds@example.com");
        request.setPassword("TempPass@123");
        request.setRoles(Set.of("ROLE_USER"));

        Role roleUser = new Role(1L, "ROLE_USER");

        when(userRepository.existsByEmail("jds@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("jnedoesmith")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("TempPass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(400L);
            return u;
        });
        when(authProviderRepository.existsByUserIdAndProviderType(400L, ProviderType.LOCAL_EMAIL)).thenReturn(false);

        adminUserService.createUser(request);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("jnedoesmith", userCaptor.getValue().getUsername());
    }

    // ── Test helpers ─────────────────────────────────────────────────────

    private User buildTestUser(Long id, String name, String username, String email, String phone) {
        Role roleUser = new Role(1L, "ROLE_USER");
        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);

        User user = User.builder()
                .name(name)
                .username(username)
                .email(email)
                .phone(phone)
                .password("encoded")
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(false)
                .accountLocked(false)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user.setId(id);
        return user;
    }
}
