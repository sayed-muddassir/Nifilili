package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.kyc.events.KycApprovedEvent;
import com.nifilili.kyc.events.KycRejectedEvent;
import com.nifilili.kyc.events.KycSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to KYC module events and updates business status accordingly.
 * This keeps the business module in control of its own entity state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessKycEventListener {

    private final BusinessRepository businessRepository;

    @EventListener
    @Transactional
    public void onKycSubmitted(KycSubmittedEvent event) {
        Business business = findBusiness(event.businessId());
        business.setStatus(BusinessStatus.PENDING);
        businessRepository.save(business);
        log.info("Business id='{}' status set to PENDING after KYC submission", event.businessId());
    }

    @EventListener
    @Transactional
    public void onKycApproved(KycApprovedEvent event) {
        Business business = findBusiness(event.businessId());
        business.setStatus(BusinessStatus.PUBLISHED);
        businessRepository.save(business);
        log.info("Business id='{}' status set to PUBLISHED after KYC approval", event.businessId());
    }

    @EventListener
    @Transactional
    public void onKycRejected(KycRejectedEvent event) {
        Business business = findBusiness(event.businessId());
        business.setStatus(BusinessStatus.DRAFT);
        businessRepository.save(business);
        log.info("Business id='{}' status set to DRAFT after KYC rejection", event.businessId());
    }

    private Business findBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
    }
}
