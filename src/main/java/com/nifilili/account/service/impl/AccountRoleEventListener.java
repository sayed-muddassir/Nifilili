package com.nifilili.account.service.impl;

import com.nifilili.auth.domain.Role;
import com.nifilili.auth.domain.User;
import com.nifilili.auth.repository.RoleRepository;
import com.nifilili.auth.repository.UserRepository;
import com.nifilili.business.events.BusinessCreatedEvent;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to cross-module events and auto-assigns roles.
 * Follows the established event listener pattern (see BusinessKycEventListener).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRoleEventListener {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;

    /**
     * Assigns ROLE_BUSINESS_OWNER to the user when they create their first business.
     */
    @EventListener
    @Transactional
    public void onBusinessCreated(BusinessCreatedEvent event) {
        Long businessId = event.businessId();
        Long ownerUserId = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId))
                .getOwnerUserId();

        User user = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerUserId));

        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> "ROLE_BUSINESS_OWNER".equals(r.getName()));

        if (!hasRole) {
            Role businessOwnerRole = roleRepository.findByName("ROLE_BUSINESS_OWNER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_BUSINESS_OWNER not seeded"));
            user.getRoles().add(businessOwnerRole);
            userRepository.save(user);
            log.info("Auto-assigned ROLE_BUSINESS_OWNER to userId={} after business creation", ownerUserId);
        }
    }

    // TODO(account): Listen to FreelancerProfileCreatedEvent when freelancer module is built
    // @EventListener
    // @Transactional
    // public void onFreelancerProfileCreated(FreelancerProfileCreatedEvent event) {
    //     // Assign ROLE_FREELANCER to the user
    // }
}
