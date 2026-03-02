package com.nifilili.offering.service.impl;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.core.enums.offering.OfferingType;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.response.PublicOfferingSummary;
import com.nifilili.offering.dto.response.PublicVariantResponse;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import com.nifilili.offering.service.PublicOfferingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicOfferingServiceImpl implements PublicOfferingService {

    private final OfferingRepository offeringRepository;
    private final OfferingVariantRepository variantRepository;
    private final OfferingVariantAttributeRepository variantAttributeRepository;

    public OfferingEntity getPublishedOffering(Long id) {
        return offeringRepository.findByIdAndStatus(id, OfferingStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));
    }

    public List<PublicVariantResponse> getVariantsWithDetails(Long offeringId) {
        List<OfferingVariantEntity> variants = variantRepository.findByOfferingIdAndStatus(
                offeringId, "ACTIVE"
        );

        return variants.stream().map(variant -> {
            Map<String, String> attributes = variantAttributeRepository
                    .findByVariantId(variant.getId()).stream()
                    .collect(Collectors.toMap(
                            OfferingVariantAttributeEntity::getAttributeName,
                            OfferingVariantAttributeEntity::getAttributeValue,
                            (existing, replacement) -> existing
                    ));

            return new PublicVariantResponse(
                    variant.getId(),
                    variant.getSku(),
                    variant.getPrice(),
                    attributes
            );
        }).toList();
    }

    public Page<PublicOfferingSummary> searchOfferings(Long categoryId,
                                                       OfferingType type,
                                                       BigDecimal minPrice,
                                                       BigDecimal maxPrice,
                                                       Pageable pageable) {
        log.debug("Searching offerings: categoryId={}, type={}, minPrice={}, maxPrice={}",
                categoryId, type, minPrice, maxPrice);

        Page<OfferingEntity> page = offeringRepository.searchPublished(
                categoryId, type, minPrice, maxPrice, pageable
        );

        return page.map(entity -> new PublicOfferingSummary(
                entity.getId(),
                entity.getTitle(),
                entity.getPrice(),
                Boolean.TRUE.equals(entity.getIsFeatured())
        ));
    }

    public Page<PublicOfferingSummary> listByOwner(Long ownerId, Pageable pageable) {
        log.debug("Listing published offerings for ownerId={}", ownerId);
        Page<OfferingEntity> page = offeringRepository.findByOwnerIdAndStatus(
                ownerId, OfferingStatus.PUBLISHED, pageable
        );
        return page.map(e -> new PublicOfferingSummary(
                e.getId(), e.getTitle(), e.getPrice(), Boolean.TRUE.equals(e.getIsFeatured())
        ));
    }

    public Page<PublicOfferingSummary> listByCategory(Long categoryId, Pageable pageable) {
        log.debug("Listing published offerings for categoryId={}", categoryId);
        Page<OfferingEntity> page = offeringRepository.findByCategoryIdAndStatus(
                categoryId, OfferingStatus.PUBLISHED, pageable
        );
        return page.map(e -> new PublicOfferingSummary(
                e.getId(), e.getTitle(), e.getPrice(), Boolean.TRUE.equals(e.getIsFeatured())
        ));
    }

    public Page<PublicOfferingSummary> listFeatured(Pageable pageable) {
        log.debug("Listing featured published offerings");
        Page<OfferingEntity> page = offeringRepository.findByIsFeaturedTrueAndStatus(
                OfferingStatus.PUBLISHED, pageable
        );
        return page.map(e -> new PublicOfferingSummary(
                e.getId(), e.getTitle(), e.getPrice(), true
        ));
    }
}

