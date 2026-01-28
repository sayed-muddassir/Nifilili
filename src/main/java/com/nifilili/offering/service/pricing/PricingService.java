package com.nifilili.offering.service.pricing;

import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.response.DiscountInfo;
import com.nifilili.offering.dto.response.PricingResponse;
import com.nifilili.offering.repository.OfferingDiscountRepository;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingService {

    private final OfferingDiscountRepository discountRepository;
    private final OfferingRepository offeringRepository;
    private final OfferingVariantRepository variantRepository;

    public BigDecimal resolveOfferingPrice(Long offeringId) {
        OfferingEntity offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        BigDecimal base = offering.getPrice();
        return applyDiscount(
                base,
                discountRepository.findFirstByOfferingIdAndVariantIsNullAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        offeringId, "ACTIVE", now(), now()
                ).orElse(null)
        );
    }

    public PricingResponse getVariantPriceDetails(Long variantId) {
        OfferingVariantEntity variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        BigDecimal base = variant.getPrice();

        OfferingDiscountEntity discount =
                discountRepository.findFirstByVariantIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        variantId, "ACTIVE", now(), now()
                ).orElse(null);

        return new PricingResponse(
                base,
                new DiscountInfo(discount.getDiscountType(), BigDecimal.valueOf(discount.getDiscountValue())),
                applyDiscount(base, discount)
        );
    }

    private BigDecimal applyDiscount(
            BigDecimal base,
            OfferingDiscountEntity discount
    ) {
        if (discount == null) return base;

        if (DiscountType.PERCENTAGE.equals(discount.getDiscountType())) {
            return base.subtract(
                    base.multiply(BigDecimal.valueOf(discount.getDiscountValue()))
                            .divide(BigDecimal.valueOf(100))
            );
        }

        return base.subtract(BigDecimal.valueOf(discount.getDiscountValue()));
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}

