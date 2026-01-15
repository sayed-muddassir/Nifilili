package com.nifilili.offering.service.owner;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.service.admin.OfferingCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final OfferingCategoryService categoryService;

    public OfferingEntity create(OfferingEntity offering, Long offeringCategoryId) {
        OfferingCategoryEntity offeringCategoryEntity = categoryService.validateLeafCategory(
                offeringCategoryId
        );

        offering.setCategory(offeringCategoryEntity);
        offering.setStatus(OfferingStatus.DRAFT);
        offering.setViewCount(0);
        offering.setCreatedAt(LocalDateTime.now());
        offering.setCreatedBy(0L); // TODO set actual owner ID
        offering.setUpdatedAt(LocalDateTime.now());
        offering.setUpdatedBy(0L); // TODO set actual owner ID

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

        return offeringRepository.save(existing);
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

