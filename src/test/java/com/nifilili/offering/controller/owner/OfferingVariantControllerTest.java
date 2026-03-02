package com.nifilili.offering.controller.owner;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.CreateVariantRequest;
import com.nifilili.offering.dto.request.UpdateVariantRequest;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.dto.response.VariantResponse;
import com.nifilili.offering.service.OfferingVariantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingVariantControllerTest {

    private static final long OFFERING_ID = 1L;
    private static final long VARIANT_ID = 50L;

    @Mock
    private OfferingVariantService variantService;

    @InjectMocks
    private OfferingVariantController controller;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenValidRequest_ShouldReturnVariantResponse() {
        CreateVariantRequest request = new CreateVariantRequest(
                "SKU-001", BigDecimal.valueOf(29.99), 100L, List.of("img.jpg")
        );

        OfferingVariantEntity saved = buildVariant();
        when(variantService.create(eq(OFFERING_ID), any(OfferingVariantEntity.class))).thenReturn(saved);

        VariantResponse result = controller.create(OFFERING_ID, request);

        assertEquals(VARIANT_ID, result.id());
        assertEquals("SKU-001", result.sku());
        assertEquals(BigDecimal.valueOf(29.99), result.price());
        assertEquals("ACTIVE", result.status());

        ArgumentCaptor<OfferingVariantEntity> captor = ArgumentCaptor.forClass(OfferingVariantEntity.class);
        verify(variantService).create(eq(OFFERING_ID), captor.capture());
        assertEquals("SKU-001", captor.getValue().getSku());
    }

    @Test
    void create_WhenOfferingNotFound_ShouldPropagateException() {
        CreateVariantRequest request = new CreateVariantRequest("SKU", BigDecimal.TEN, 10L, List.of());
        when(variantService.create(eq(OFFERING_ID), any(OfferingVariantEntity.class)))
                .thenThrow(new IllegalArgumentException("Offering not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.create(OFFERING_ID, request));
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenValid_ShouldReturnUpdatedResponse() {
        UpdateVariantRequest request = new UpdateVariantRequest(
                "SKU-UPD", BigDecimal.valueOf(39.99), 200L, List.of("new.jpg")
        );

        OfferingVariantEntity updated = buildVariant();
        updated.setSku("SKU-UPD");
        updated.setPrice(BigDecimal.valueOf(39.99));
        when(variantService.update(eq(VARIANT_ID), any(OfferingVariantEntity.class))).thenReturn(updated);

        VariantResponse result = controller.update(OFFERING_ID, VARIANT_ID, request);

        assertEquals("SKU-UPD", result.sku());
        assertEquals(BigDecimal.valueOf(39.99), result.price());
    }

    @Test
    void update_WhenNotFound_ShouldPropagateException() {
        UpdateVariantRequest request = new UpdateVariantRequest("SKU", BigDecimal.TEN, 10L, List.of());
        when(variantService.update(eq(VARIANT_ID), any(OfferingVariantEntity.class)))
                .thenThrow(new IllegalArgumentException("Variant not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(OFFERING_ID, VARIANT_ID, request));
    }

    // ── list ─────────────────────────────────────────────────────────

    @Test
    void list_WhenCalled_ShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingVariantEntity variant = buildVariant();
        Page<OfferingVariantEntity> page = new PageImpl<>(List.of(variant));

        when(variantService.listPaginated(OFFERING_ID, pageable)).thenReturn(page);

        Page<VariantResponse> result = controller.list(OFFERING_ID, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(VARIANT_ID, result.getContent().get(0).id());
    }

    @Test
    void list_WhenEmpty_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(variantService.listPaginated(OFFERING_ID, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<VariantResponse> result = controller.list(OFFERING_ID, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── inventory ────────────────────────────────────────────────────

    @Test
    void inventory_WhenCalled_ShouldDelegateAndReturnUpdated() {
        StatusResponse result = controller.inventory(VARIANT_ID, 500L);

        assertEquals("UPDATED", result.status());
        verify(variantService).updateInventory(VARIANT_ID, 500L);
    }

    // ── deactivate ───────────────────────────────────────────────────

    @Test
    void deactivate_WhenCalled_ShouldReturnInactiveStatus() {
        StatusResponse result = controller.deactivate(OFFERING_ID, VARIANT_ID);

        assertEquals("INACTIVE", result.status());
        verify(variantService).deactivate(VARIANT_ID);
    }

    @Test
    void deactivate_WhenNotFound_ShouldPropagateException() {
        doThrow(new IllegalArgumentException("Variant not found"))
                .when(variantService).deactivate(VARIANT_ID);

        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivate(OFFERING_ID, VARIANT_ID));
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingVariantEntity buildVariant() {
        OfferingVariantEntity entity = new OfferingVariantEntity();
        TestEntityIdUtil.withId(entity, VARIANT_ID);
        entity.setSku("SKU-001");
        entity.setPrice(BigDecimal.valueOf(29.99));
        entity.setAvailableQuantity(100L);
        entity.setStatus("ACTIVE");
        entity.setImages(List.of("img.jpg"));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(100L);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(100L);
        return entity;
    }
}
