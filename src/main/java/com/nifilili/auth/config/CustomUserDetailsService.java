package com.nifilili.auth.config;

import com.nifilili.auth.UserPrincipal;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.PermissionRepository;
import com.nifilili.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));

        // Collect role IDs for permission lookup
        Set<Long> roleIds = user.getRoles().stream()
                .map(role -> role.getId())
                .collect(Collectors.toSet());

        // Authorities = role names (ROLE_USER, ROLE_ADMIN, ...) + permission names (BUSINESS_CREATE, ...)
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities
        user.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority(role.getName())));

        // Add permission-based authorities
        if (!roleIds.isEmpty()) {
            Set<String> permissionNames = permissionRepository.findPermissionNamesByRoleIds(roleIds);
            permissionNames.forEach(perm ->
                    authorities.add(new SimpleGrantedAuthority(perm)));
        }

        log.info("Loaded user '{}' with {} authorities ({} roles + {} permissions)",
                user.getUsername(), authorities.size(),
                user.getRoles().size(), authorities.size() - user.getRoles().size());

        return UserPrincipal.of(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                user.isEmailVerified(),
                user.isAccountLocked(),
                authorities
        );
    }
}
