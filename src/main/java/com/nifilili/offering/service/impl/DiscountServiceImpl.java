package com.nifilili.offering.service.impl;

import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.CreateDiscountRequest;
import com.nifilili.offering.dto.response.DiscountDetailResponse;
import com.nifilili.offering.repository.OfferingDiscountRepository;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import com.nifilili.offering.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscountServiceImpl implements DiscountService {

    private final OfferingDiscountRepository discountRepository;
    private final OfferingRepository offeringRepository;
    private final OfferingVariantRepository variantRepository;

    public OfferingDiscountEntity create(CreateDiscountRequest request) {

        OfferingDiscountEntity discountEntity = new OfferingDiscountEntity();

        OfferingEntity offeringEntity = offeringRepository.findById(request.offeringId())
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        discountEntity.setOffering(offeringEntity);

        if (request.variantId() != null) {
            OfferingVariantEntity offeringVariantEntity = variantRepository.findById(request.variantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
            discountEntity.setVariant(offeringVariantEntity);
        }

        discountEntity.setDiscountType(request.discountType());
        discountEntity.setDiscountValue(request.discountValue());
        discountEntity.setStartDate(request.startDate());
        discountEntity.setEndDate(request.endDate());

        discountEntity.setStatus("ACTIVE");
        return discountRepository.save(discountEntity);
    }

    @Transactional(readOnly = true)
    public Page<DiscountDetailResponse> listByOffering(Long offeringId, Pageable pageable) {
        log.debug("Listing discounts for offeringId={}", offeringId);
        Page<OfferingDiscountEntity> page = discountRepository.findByOfferingId(offeringId, pageable);
        return page.map(d -> new DiscountDetailResponse(
                d.getId(),
                d.getOffering().getId(),
                d.getVariant() != null ? d.getVariant().getId() : null,
                d.getDiscountType(),
                d.getDiscountValue(),
                d.getStartDate(),
                d.getEndDate(),
                d.getStatus()
        ));
    }

    public void deactivate(Long discountId) {
        log.info("Deactivating discount id={}", discountId);
        OfferingDiscountEntity discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found"));
        discount.setStatus("INACTIVE");
        discountRepository.save(discount);
    }
}

