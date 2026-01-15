package com.nifilili.offering.service.publicapi;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicOfferingService {

    private final OfferingRepository offeringRepository;
    private final OfferingVariantRepository variantRepository;

    public OfferingEntity getPublishedOffering(Long id) {
        return offeringRepository.findByIdAndStatus(id, OfferingStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));
    }

    public List<OfferingVariantEntity> getVariants(Long offeringId) {
        return variantRepository.findByOfferingIdAndStatus(
                offeringId, "ACTIVE"
        );
    }
}

