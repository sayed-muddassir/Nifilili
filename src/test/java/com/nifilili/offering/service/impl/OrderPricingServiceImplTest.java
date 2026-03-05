package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.response.PricingResponse;
import com.nifilili.offering.repository.OfferingDiscountRepository;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPricingServiceImplTest {

    private static final long OFFERING_ID = 1L;
    private static final long VARIANT_ID = 50L;

    @Mock
    private OfferingDiscountRepository discountRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private OfferingVariantRepository variantRepository;

    @InjectMocks
    private PricingServiceImpl pricingService;

    // ── resolveOfferingPrice ─────────────────────────────────────────

    @Test
    void resolveOfferingPrice_WhenNoDiscount_ShouldReturnBasePrice() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);
        offering.setPrice(BigDecimal.valueOf(100));

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(discountRepository.findFirstByOfferingIdAndVariantIsNullAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(OFFERING_ID), eq("ACTIVE"), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        BigDecimal result = pricingService.resolveOfferingPrice(OFFERING_ID);

        assertEquals(0, BigDecimal.valueOf(100).compareTo(result));
    }

    @Test
    void resolveOfferingPrice_WhenPercentageDiscount_ShouldApplyPercentage() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);
        offering.setPrice(BigDecimal.valueOf(200));

        OfferingDiscountEntity discount = new OfferingDiscountEntity();
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setDiscountValue(25L); // 25%

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(discountRepository.findFirstByOfferingIdAndVariantIsNullAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(OFFERING_ID), eq("ACTIVE"), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.of(discount));

        BigDecimal result = pricingService.resolveOfferingPrice(OFFERING_ID);

        // 200 - (200 * 25 / 100) = 200 - 50 = 150
        assertEquals(0, BigDecimal.valueOf(150).compareTo(result));
    }

    @Test
    void resolveOfferingPrice_WhenFixedDiscount_ShouldSubtractFixedAmount() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);
        offering.setPrice(BigDecimal.valueOf(100));

        OfferingDiscountEntity discount = new OfferingDiscountEntity();
        discount.setDiscountType(DiscountType.FIXED_AMOUNT);
        discount.setDiscountValue(30L);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(discountRepository.findFirstByOfferingIdAndVariantIsNullAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(OFFERING_ID), eq("ACTIVE"), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.of(discount));

        BigDecimal result = pricingService.resolveOfferingPrice(OFFERING_ID);

        // 100 - 30 = 70
        assertEquals(0, BigDecimal.valueOf(70).compareTo(result));
    }

    @Test
    void resolveOfferingPrice_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pricingService.resolveOfferingPrice(OFFERING_ID));
        assertEquals("Offering not found", ex.getMessage());
    }

    // ── getVariantPriceDetails ───────────────────────────────────────

    @Test
    void getVariantPriceDetails_WhenDiscountExists_ShouldReturnPricingResponse() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);
        variant.setPrice(BigDecimal.valueOf(50));

        OfferingDiscountEntity discount = new OfferingDiscountEntity();
        discount.setDiscountType(DiscountType.PERCENTAGE);
        discount.setDiscountValue(20L);

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(discountRepository.findFirstByVariantIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(VARIANT_ID), eq("ACTIVE"), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.of(discount));

        PricingResponse result = pricingService.getVariantPriceDetails(VARIANT_ID);

        assertEquals(0, BigDecimal.valueOf(50).compareTo(result.basePrice()));
        assertEquals(DiscountType.PERCENTAGE, result.discount().type());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(result.discount().value()));
        // 50 - (50 * 20 / 100) = 50 - 10 = 40
        assertEquals(0, BigDecimal.valueOf(40).compareTo(result.finalPrice()));
    }

    @Test
    void getVariantPriceDetails_WhenVariantNotFound_ShouldThrow() {
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pricingService.getVariantPriceDetails(VARIANT_ID));
        assertEquals("Variant not found", ex.getMessage());
    }
}
