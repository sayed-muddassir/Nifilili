package com.nifilili.auth.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthDomainModelTest {

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

//    @Test
//    void user_ConstructorsAndSetters_ShouldWork() {
//        Role role = new Role(1L, "ROLE_USER");
//        User user = new User();
//        user.setId(11L);
//        user.setName("John");
//        user.setUsername("john");
//        user.setEmail("john@example.com");
//        user.setPassword("hashed");
//        user.setRoles(Set.of(role));
//
//        assertEquals(11L, user.getId());
//        assertEquals("John", user.getName());
//        assertEquals("john", user.getUsername());
//        assertEquals("john@example.com", user.getEmail());
//        assertEquals("hashed", user.getPassword());
//        assertEquals(1, user.getRoles().size());
//
//        User userFromAllArgs = new User(22L, "Admin", "admin", "admin@example.com", "pwd", Set.of(role));
//        assertEquals(22L, userFromAllArgs.getId());
//        assertEquals("Admin", userFromAllArgs.getName());
//    }
}

