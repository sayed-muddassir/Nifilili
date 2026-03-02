package com.nifilili.offering.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import com.nifilili.offering.service.OfferingVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OfferingVariantServiceImpl implements OfferingVariantService {

    private final OfferingVariantRepository variantRepository;
    private final OfferingRepository offeringRepository;

    public OfferingVariantEntity create(Long offeringId, OfferingVariantEntity variant) {
        OfferingEntity offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        variant.setOffering(offering);
        variant.setStatus("ACTIVE");
        variant.setCreatedAt(LocalDateTime.now());
        variant.setCreatedBy(currentUserId);
        variant.setUpdatedAt(LocalDateTime.now());
        variant.setUpdatedBy(currentUserId);

        return variantRepository.save(variant);
    }

    public OfferingVariantEntity update(Long variantId, OfferingVariantEntity updates) {
        OfferingVariantEntity existing = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        existing.setSku(updates.getSku());
        existing.setPrice(updates.getPrice());
        existing.setAvailableQuantity(updates.getAvailableQuantity());
        existing.setImages(updates.getImages());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SecurityUtil.getCurrentUserId());

        return variantRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<OfferingVariantEntity> list(Long offeringId) {
        return variantRepository.findByOfferingId(offeringId);
    }

    @Transactional(readOnly = true)
    public Page<OfferingVariantEntity> listPaginated(Long offeringId, Pageable pageable) {
        return variantRepository.findByOfferingId(offeringId, pageable);
    }

    public void updateInventory(Long variantId, Long quantity) {
        variantRepository.updateInventory(variantId, quantity);
    }

    public void deactivate(Long variantId) {
        log.info("Deactivating variant id={}", variantId);
        OfferingVariantEntity variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        variant.setStatus("INACTIVE");
        variant.setUpdatedAt(LocalDateTime.now());
        variant.setUpdatedBy(SecurityUtil.getCurrentUserId());
        variantRepository.save(variant);
    }
}
