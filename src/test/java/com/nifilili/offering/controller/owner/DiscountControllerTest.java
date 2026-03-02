package com.nifilili.offering.controller.owner;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.dto.request.CreateDiscountRequest;
import com.nifilili.offering.dto.response.DiscountDetailResponse;
import com.nifilili.offering.dto.response.DiscountResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountControllerTest {

    private static final long OFFERING_ID = 1L;
    private static final long DISCOUNT_ID = 200L;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private DiscountController controller;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenValid_ShouldReturnDiscountResponse() {
        LocalDateTime now = LocalDateTime.now();
        CreateDiscountRequest request = new CreateDiscountRequest(
                OFFERING_ID, null, DiscountType.PERCENTAGE, 10L, now, now.plusDays(30)
        );

        OfferingDiscountEntity saved = new OfferingDiscountEntity();
        TestEntityIdUtil.withId(saved, DISCOUNT_ID);
        saved.setStatus("ACTIVE");

        when(discountService.create(request)).thenReturn(saved);

        DiscountResponse result = controller.create(request);

        assertEquals(DISCOUNT_ID, result.id());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void create_WhenOfferingNotFound_ShouldPropagateException() {
        CreateDiscountRequest request = new CreateDiscountRequest(
                OFFERING_ID, null, DiscountType.PERCENTAGE, 10L,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );
        when(discountService.create(request))
                .thenThrow(new IllegalArgumentException("Offering not found"));

        assertThrows(IllegalArgumentException.class, () -> controller.create(request));
    }

    // ── listByOffering ───────────────────────────────────────────────

    @Test
    void listByOffering_WhenCalled_ShouldDelegateToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DiscountDetailResponse> page = new PageImpl<>(List.of(
                new DiscountDetailResponse(DISCOUNT_ID, OFFERING_ID, null,
                        DiscountType.PERCENTAGE, 10L,
                        LocalDateTime.now(), LocalDateTime.now().plusDays(30), "ACTIVE")
        ));
        when(discountService.listByOffering(OFFERING_ID, pageable)).thenReturn(page);

        Page<DiscountDetailResponse> result = controller.listByOffering(OFFERING_ID, pageable);

        assertEquals(1, result.getTotalElements());
        verify(discountService).listByOffering(OFFERING_ID, pageable);
    }

    @Test
    void listByOffering_WhenEmpty_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(discountService.listByOffering(OFFERING_ID, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<DiscountDetailResponse> result = controller.listByOffering(OFFERING_ID, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── deactivate ───────────────────────────────────────────────────

    @Test
    void deactivate_WhenCalled_ShouldReturnInactiveStatus() {
        StatusResponse result = controller.deactivate(DISCOUNT_ID);

        assertEquals("INACTIVE", result.status());
        verify(discountService).deactivate(DISCOUNT_ID);
    }

    @Test
    void deactivate_WhenNotFound_ShouldPropagateException() {
        doThrow(new IllegalArgumentException("Discount not found"))
                .when(discountService).deactivate(DISCOUNT_ID);

        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivate(DISCOUNT_ID));
    }
}
