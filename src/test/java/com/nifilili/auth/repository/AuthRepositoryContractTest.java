package com.nifilili.auth.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AuthRepositoryContractTest {

    @Test
    void userRepository_ShouldExtendJpaRepositoryAndExposeLookupMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(UserRepository.class));

        Method method = Arrays.stream(UserRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByUsernameOrEmail"))
                .findFirst()
                .orElse(null);

        assertNotNull(method);
        assertEquals(2, method.getParameterCount());
    }

    @Test
    void roleRepository_ShouldExtendJpaRepositoryAndExposeFindByNameMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(RoleRepository.class));

        Method method = Arrays.stream(RoleRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByName"))
                .findFirst()
                .orElse(null);

        assertNotNull(method);
        assertEquals(1, method.getParameterCount());
    }

    @Test
    void otpTokenRepository_ShouldExtendJpaRepositoryAndExposeFinderMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(OtpTokenRepository.class));

        boolean hasFinderMethod = Arrays.stream(OtpTokenRepository.class.getMethods())
                .anyMatch(m -> m.getName().startsWith("findFirstByIdentifier"));

        assertTrue(hasFinderMethod);
    }

    @Test
    void refreshTokenRepository_ShouldExtendJpaRepositoryAndExposeFindByTokenMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(RefreshTokenRepository.class));

        Method method = Arrays.stream(RefreshTokenRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByToken"))
                .findFirst()
                .orElse(null);

        assertNotNull(method);
        assertEquals(1, method.getParameterCount());
    }

    @Test
    void loginAttemptRepository_ShouldExtendJpaRepositoryAndExposeCountMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(LoginAttemptRepository.class));

        boolean hasCountMethod = Arrays.stream(LoginAttemptRepository.class.getMethods())
                .anyMatch(m -> m.getName().equals("countRecentFailedAttempts"));

        assertTrue(hasCountMethod);
    }

    @Test
    void loginHistoryRepository_ShouldExtendJpaRepositoryAndExposeFinderMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(LoginHistoryRepository.class));

        boolean hasFinderMethod = Arrays.stream(LoginHistoryRepository.class.getMethods())
                .anyMatch(m -> m.getName().equals("findByUserIdOrderByLoggedInAtDesc"));

        assertTrue(hasFinderMethod);
    }

    @Test
    void permissionRepository_ShouldExtendJpaRepositoryAndExposeQueryMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(PermissionRepository.class));

        boolean hasQueryMethod = Arrays.stream(PermissionRepository.class.getMethods())
                .anyMatch(m -> m.getName().equals("findPermissionNamesByRoleIds"));

        assertTrue(hasQueryMethod);
    }

    @Test
    void userAuthProviderRepository_ShouldExtendJpaRepositoryAndExposeFinderMethod() {
        assertTrue(JpaRepository.class.isAssignableFrom(UserAuthProviderRepository.class));

        boolean hasFinderMethod = Arrays.stream(UserAuthProviderRepository.class.getMethods())
                .anyMatch(m -> m.getName().equals("findByUserIdAndProviderType"));

        assertTrue(hasFinderMethod);
    }
}

