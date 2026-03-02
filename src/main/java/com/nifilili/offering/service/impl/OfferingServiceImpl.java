package com.nifilili.offering.service.impl;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.service.OfferingCategoryService;
import com.nifilili.offering.service.OfferingService;
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
public class OfferingServiceImpl implements OfferingService {

    private final OfferingRepository offeringRepository;
    private final OfferingCategoryService categoryService;

    @Transactional(readOnly = true)
    public Page<OfferingResponse> listMyOfferings(OfferingStatus status, Pageable pageable) {
        Long ownerId = SecurityUtil.getCurrentUserId();
        log.info("Listing offerings for owner={}, status={}", ownerId, status);

        Page<OfferingEntity> page = (status == null)
                ? offeringRepository.findByOwnerId(ownerId, pageable)
                : offeringRepository.findByOwnerIdAndStatus(ownerId, status, pageable);

        return page.map(e -> new OfferingResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                e.getPrice(),
                e.getImages()
        ));
    }

    public OfferingEntity create(OfferingEntity offering, Long offeringCategoryId) {
        OfferingCategoryEntity offeringCategoryEntity = categoryService.validateLeafCategory(
                offeringCategoryId
        );

        offering.setCategory(offeringCategoryEntity);
        offering.setStatus(OfferingStatus.DRAFT);
        offering.setViewCount(0);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        offering.setCreatedAt(LocalDateTime.now());
        offering.setCreatedBy(currentUserId);
        offering.setUpdatedAt(LocalDateTime.now());
        offering.setUpdatedBy(currentUserId);

        return offeringRepository.save(offering);
    }

    public OfferingEntity update(Long id, OfferingEntity updates) {
        OfferingEntity existing = offeringRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        if (OfferingStatus.ARCHIVED.equals(existing.getStatus())) {
            throw new IllegalStateException("Archived offering cannot be modified");
        }

        existing.setTitle(updates.getTitle());
        existing.setDescription(updates.getDescription());
        existing.setPrice(updates.getPrice());
        existing.setAvailableQuantity(updates.getAvailableQuantity());
        existing.setIsFeatured(updates.getIsFeatured());
        existing.setIsB2bEnabled(updates.getIsB2bEnabled());
        existing.setImages(updates.getImages());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SecurityUtil.getCurrentUserId());

        return offeringRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public OfferingEntity getMyOffering(Long id) {
        Long ownerId = SecurityUtil.getCurrentUserId();
        OfferingEntity offering = get(id);
        if (!offering.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Offering not found");
        }
        return offering;
    }

    public void publish(Long id) {
        OfferingEntity offering = get(id);
        offering.setStatus(OfferingStatus.PUBLISHED);
    }

    public void archive(Long id) {
        OfferingEntity offering = get(id);
        offering.setStatus(OfferingStatus.ARCHIVED);
    }

    public void restore(Long id) {
        OfferingEntity offering = get(id);
        offering.setStatus(OfferingStatus.DRAFT);
    }

    public void updateInventory(Long id, Integer quantity) {
        offeringRepository.updateInventory(id, quantity);
    }

    public OfferingEntity get(Long id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));
    }
}

