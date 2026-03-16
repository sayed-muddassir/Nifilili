package com.nifilili.auth.domain;

import com.nifilili.auth.domain.enums.IdentifierType;
import com.nifilili.auth.domain.enums.OtpPurpose;
import com.nifilili.auth.domain.enums.ProviderType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthDomainModelTest {

    // ── Role ──────────────────────────────────────────────────────────────

    @Test
    void role_ConstructorsAndSetters_ShouldWork() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        assertEquals(1L, role.getId());
        assertEquals("ROLE_USER", role.getName());

        Role roleFromAllArgs = new Role(2L, "ROLE_ADMIN");
        assertEquals(2L, roleFromAllArgs.getId());
        assertEquals("ROLE_ADMIN", roleFromAllArgs.getName());
    }

    // ── User ──────────────────────────────────────────────────────────────

    @Test
    void user_ConstructorsAndSetters_ShouldWork() {
        Role role = new Role(1L, "ROLE_USER");
        User user = new User();
        user.setId(11L);
        user.setName("John");
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("hashed");
        user.setPhone("+977-9800000000");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);
        user.setAccountLocked(false);
        user.setRoles(Set.of(role));

        assertEquals(11L, user.getId());
        assertEquals("John", user.getName());
        assertEquals("john", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashed", user.getPassword());
        assertEquals("+977-9800000000", user.getPhone());
        assertTrue(user.isEnabled());
        assertTrue(user.isEmailVerified());
        assertFalse(user.isPhoneVerified());
        assertFalse(user.isAccountLocked());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void user_SixArgConstructor_ShouldWork() {
        Role role = new Role(1L, "ROLE_USER");
        User user = new User(22L, "Admin", "admin", "admin@example.com", "pwd", Set.of(role));

        assertEquals(22L, user.getId());
        assertEquals("Admin", user.getName());
        assertEquals("admin", user.getUsername());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("pwd", user.getPassword());
    }

    @Test
    void user_OnCreate_ShouldSetTimestamps() {
        User user = new User();
        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void user_OnUpdate_ShouldUpdateTimestamp() {
        User user = new User();
        user.onCreate();

        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void user_SetId_ShouldSetProtectedIdField() {
        User user = new User();
        user.setId(99L);
        assertEquals(99L, user.getId());
    }

    // ── OtpToken ──────────────────────────────────────────────────────────

    @Test
    void otpToken_BuilderAndGetters_ShouldWork() {
        OtpToken token = OtpToken.builder()
                .identifier("+977-9800000000")
                .identifierType(IdentifierType.PHONE)
                .otp("123456")
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals("+977-9800000000", token.getIdentifier());
        assertEquals(IdentifierType.PHONE, token.getIdentifierType());
        assertEquals("123456", token.getOtp());
        assertEquals(OtpPurpose.LOGIN, token.getPurpose());
        assertFalse(token.isUsed());
        assertEquals(0, token.getAttempts());
    }

    @Test
    void otpToken_IsExpired_WhenExpiresAtInPast_ShouldReturnTrue() {
        OtpToken token = OtpToken.builder()
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        assertTrue(token.isExpired());
    }

    @Test
    void otpToken_IsExpired_WhenExpiresAtInFuture_ShouldReturnFalse() {
        OtpToken token = OtpToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        assertFalse(token.isExpired());
    }

    @Test
    void otpToken_IsUsable_WhenNotUsedAndNotExpired_ShouldReturnTrue() {
        OtpToken token = OtpToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        assertTrue(token.isUsable());
    }

    @Test
    void otpToken_IsUsable_WhenUsed_ShouldReturnFalse() {
        OtpToken token = OtpToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(true)
                .build();

        assertFalse(token.isUsable());
    }

    @Test
    void otpToken_IncrementAttempts_ShouldIncrementByOne() {
        OtpToken token = OtpToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .build();

        token.incrementAttempts();
        assertEquals(1, token.getAttempts());

        token.incrementAttempts();
        assertEquals(2, token.getAttempts());
    }

    @Test
    void otpToken_SetId_ShouldWork() {
        OtpToken token = OtpToken.builder().build();
        token.setId(42L);
        assertEquals(42L, token.getId());
    }

    // ── RefreshToken ──────────────────────────────────────────────────────

    @Test
    void refreshToken_IsExpired_WhenExpiresAtInPast_ShouldReturnTrue() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        assertTrue(token.isExpired());
    }

    @Test
    void refreshToken_IsExpired_WhenExpiresAtInFuture_ShouldReturnFalse() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        assertFalse(token.isExpired());
    }

    @Test
    void refreshToken_IsUsable_WhenNotRevokedAndNotExpired_ShouldReturnTrue() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();

        assertTrue(token.isUsable());
    }

    @Test
    void refreshToken_IsUsable_WhenRevoked_ShouldReturnFalse() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(true)
                .build();

        assertFalse(token.isUsable());
    }

    @Test
    void refreshToken_SetId_ShouldWork() {
        RefreshToken token = RefreshToken.builder().build();
        token.setId(7L);
        assertEquals(7L, token.getId());
    }

    // ── LoginAttempt ──────────────────────────────────────────────────────

    @Test
    void loginAttempt_BuilderAndGetters_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        LoginAttempt attempt = LoginAttempt.builder()
                .userId(1L)
                .username("john")
                .ipAddress("192.168.1.1")
                .success(true)
                .attemptedAt(now)
                .build();

        assertEquals(1L, attempt.getUserId());
        assertEquals("john", attempt.getUsername());
        assertEquals("192.168.1.1", attempt.getIpAddress());
        assertTrue(attempt.isSuccess());
        assertEquals(now, attempt.getAttemptedAt());
    }

    // ── LoginHistory ──────────────────────────────────────────────────────

    @Test
    void loginHistory_BuilderAndGetters_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        LoginHistory history = LoginHistory.builder()
                .userId(1L)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .deviceName("Chrome")
                .loggedInAt(now)
                .build();

        assertEquals(1L, history.getUserId());
        assertEquals("192.168.1.1", history.getIpAddress());
        assertEquals("Mozilla/5.0", history.getUserAgent());
        assertEquals("Chrome", history.getDeviceName());
        assertEquals(now, history.getLoggedInAt());
    }

    // ── UserAuthProvider ──────────────────────────────────────────────────

    @Test
    void userAuthProvider_BuilderAndGetters_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        UserAuthProvider provider = UserAuthProvider.builder()
                .userId(1L)
                .providerType(ProviderType.LOCAL_EMAIL)
                .providerUserId("user@email.com")
                .linkedAt(now)
                .build();

        assertEquals(1L, provider.getUserId());
        assertEquals(ProviderType.LOCAL_EMAIL, provider.getProviderType());
        assertEquals("user@email.com", provider.getProviderUserId());
        assertEquals(now, provider.getLinkedAt());
    }

    @Test
    void userAuthProvider_SetId_ShouldWork() {
        UserAuthProvider provider = UserAuthProvider.builder().build();
        provider.setId(5L);
        assertEquals(5L, provider.getId());
    }

    // ── PermissionEntity ──────────────────────────────────────────────────

    @Test
    void permissionEntity_SettersAndGetters_ShouldWork() {
        PermissionEntity perm = new PermissionEntity();
        perm.setId(1L);
        perm.setName("READ_USERS");

        assertEquals(1L, perm.getId());
        assertEquals("READ_USERS", perm.getName());
    }

    @Test
    void permissionEntity_AllArgsConstructor_ShouldWork() {
        PermissionEntity perm = new PermissionEntity(2L, "WRITE_USERS");

        assertEquals(2L, perm.getId());
        assertEquals("WRITE_USERS", perm.getName());
    }
}
