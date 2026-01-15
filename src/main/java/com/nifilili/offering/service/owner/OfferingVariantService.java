package com.nifilili.offering.service.owner;

import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferingVariantService {

    private final OfferingVariantRepository variantRepository;
    private final OfferingRepository offeringRepository;

    public OfferingVariantEntity create(Long offeringId, OfferingVariantEntity variant) {
        OfferingEntity offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        variant.setOffering(offering);
        variant.setStatus("ACTIVE");
        variant.setCreatedAt(LocalDateTime.now());
        variant.setCreatedBy(0L); // TODO set actual owner ID
        variant.setUpdatedAt(LocalDateTime.now());
        variant.setUpdatedBy(0L); // TODO set actual owner ID

        return variantRepository.save(variant);
    }

    @Transactional(readOnly = true)
    public List<OfferingVariantEntity> list(Long offeringId) {
        return variantRepository.findByOfferingId(offeringId);
    }

    public void updateInventory(Long variantId, Long quantity) {
        variantRepository.updateInventory(variantId, quantity);
    }
}

