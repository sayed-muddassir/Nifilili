package com.nifilili.auth.config;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.PermissionRepository;
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

    @Mock private UserRepository userRepository;
    @Mock private PermissionRepository permissionRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserPrincipalWithRolesAndPermissions() {
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name("John").username("john").email("john@example.com")
                .password("hashed").enabled(true).emailVerified(true).accountLocked(false)
                .roles(Set.of(role)).build();
        user.setId(1L);

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));
        when(permissionRepository.findPermissionNamesByRoleIds(Set.of(1L)))
                .thenReturn(Set.of("BUSINESS_CREATE", "ACCOUNT_MANAGE_OWN"));

        UserDetails result = customUserDetailsService.loadUserByUsername("john");

        assertInstanceOf(UserPrincipal.class, result);
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals(1L, principal.getUserId());
        assertEquals("john", principal.getUsername());
        assertEquals("hashed", principal.getPassword());
        assertTrue(principal.isEnabled());
        assertTrue(principal.isEmailVerified());
        assertTrue(principal.isAccountNonLocked());
        // 1 role + 2 permissions = 3 authorities
        assertEquals(3, principal.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_WhenAccountLocked_ShouldReturnLockedPrincipal() {
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .name("Locked").username("locked").email("locked@example.com")
                .password("hashed").enabled(true).emailVerified(false).accountLocked(true)
                .roles(Set.of(role)).build();
        user.setId(2L);

        when(userRepository.findByUsernameOrEmail("locked", "locked"))
                .thenReturn(Optional.of(user));
        when(permissionRepository.findPermissionNamesByRoleIds(Set.of(1L)))
                .thenReturn(Set.of());

        UserDetails result = customUserDetailsService.loadUserByUsername("locked");

        UserPrincipal principal = (UserPrincipal) result;
        assertFalse(principal.isAccountNonLocked());
        assertFalse(principal.isEmailVerified());
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
