package com.nifilili.offering.controller.publicapi;

import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.offering.dto.response.DiscountInfo;
import com.nifilili.offering.dto.response.PricingResponse;
import com.nifilili.offering.service.PricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingControllerTest {

    private static final long VARIANT_ID = 50L;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private PricingController controller;

    // ── variantPrice ─────────────────────────────────────────────────

    @Test
    void variantPrice_WhenVariantExists_ShouldReturnPricingResponse() {
        PricingResponse expected = new PricingResponse(
                BigDecimal.valueOf(100),
                new DiscountInfo(DiscountType.PERCENTAGE, BigDecimal.valueOf(20)),
                BigDecimal.valueOf(80)
        );
        when(pricingService.getVariantPriceDetails(VARIANT_ID)).thenReturn(expected);

        PricingResponse result = controller.variantPrice(VARIANT_ID);

        assertEquals(BigDecimal.valueOf(100), result.basePrice());
        assertEquals(BigDecimal.valueOf(80), result.finalPrice());
        assertEquals(DiscountType.PERCENTAGE, result.discount().type());
        verify(pricingService).getVariantPriceDetails(VARIANT_ID);
    }

    @Test
    void variantPrice_WhenVariantNotFound_ShouldPropagateException() {
        when(pricingService.getVariantPriceDetails(VARIANT_ID))
                .thenThrow(new IllegalArgumentException("Variant not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.variantPrice(VARIANT_ID));
    }
}
