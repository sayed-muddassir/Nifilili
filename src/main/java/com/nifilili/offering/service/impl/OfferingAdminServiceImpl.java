package com.nifilili.offering.service.impl;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.service.OfferingAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OfferingAdminServiceImpl implements OfferingAdminService {

    private final OfferingRepository offeringRepository;

    @Transactional(readOnly = true)
    public Page<OfferingEntity> listAll(OfferingStatus status, Pageable pageable) {
        log.debug("Admin listing offerings, status={}", status);
        if (status != null) {
            return offeringRepository.findByStatus(status, pageable);
        }
        return offeringRepository.findAll(pageable);
    }

    public OfferingEntity changeStatus(Long id, OfferingStatus newStatus) {
        log.info("Admin changing offering id={} status to {}", id, newStatus);
        OfferingEntity offering = offeringRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));
        offering.setStatus(newStatus);
        offering.setUpdatedAt(LocalDateTime.now());
        return offeringRepository.save(offering);
    }
}
