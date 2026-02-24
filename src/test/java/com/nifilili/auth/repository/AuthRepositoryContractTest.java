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
}

