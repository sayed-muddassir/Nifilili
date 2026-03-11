package com.nifilili.account.service.impl;

import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.business.domain.Business;
import com.nifilili.business.events.BusinessCreatedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountRoleEventListenerTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private BusinessRepository businessRepository;

    @InjectMocks private AccountRoleEventListener listener;

    @Test
    void onBusinessCreated_WhenUserDoesNotHaveRole_ShouldAssignBusinessOwnerRole() {
        Business business = Business.builder().ownerUserId(1L).build();
        business.setId(10L);

        Role userRole = new Role(2L, "ROLE_USER");
        Role businessOwnerRole = new Role(3L, "ROLE_BUSINESS_OWNER");

        User user = User.builder().username("john").roles(new HashSet<>(Set.of(userRole))).build();
        user.setId(1L);

        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_BUSINESS_OWNER")).thenReturn(Optional.of(businessOwnerRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        listener.onBusinessCreated(new BusinessCreatedEvent(10L));

        assertTrue(user.getRoles().contains(businessOwnerRole));
        verify(userRepository).save(user);
    }

    @Test
    void onBusinessCreated_WhenUserAlreadyHasRole_ShouldNotDuplicate() {
        Business business = Business.builder().ownerUserId(1L).build();
        business.setId(10L);

        Role businessOwnerRole = new Role(3L, "ROLE_BUSINESS_OWNER");
        User user = User.builder().username("john").roles(new HashSet<>(Set.of(businessOwnerRole))).build();
        user.setId(1L);

        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        listener.onBusinessCreated(new BusinessCreatedEvent(10L));

        verify(userRepository, never()).save(any());
    }

    @Test
    void onBusinessCreated_WhenBusinessNotFound_ShouldThrowResourceNotFoundException() {
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> listener.onBusinessCreated(new BusinessCreatedEvent(999L)));
    }

    @Test
    void onBusinessCreated_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        Business business = Business.builder().ownerUserId(99L).build();
        business.setId(10L);

        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> listener.onBusinessCreated(new BusinessCreatedEvent(10L)));
    }
}
