package com.nifilili.auth.config;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserPrincipal() {
        User user = new User(
                1L,
                "John",
                "john",
                "john@example.com",
                "hashed",
                Set.of(new Role(1L, "ROLE_USER"))
        );
        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("john");

        assertInstanceOf(UserPrincipal.class, result);
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals(1L, principal.getUserId());
        assertEquals("john", principal.getUsername());
        assertEquals("hashed", principal.getPassword());
        assertEquals(1, principal.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_WhenUserMissing_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsernameOrEmail("missing", "missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing")
        );

        verify(userRepository, times(1))
                .findByUsernameOrEmail("missing", "missing");
    }
}

