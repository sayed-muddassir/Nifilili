package com.nifilili.offering.controller.publicapi;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.core.enums.offering.OfferingType;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.PublicOfferingResponse;
import com.nifilili.offering.dto.response.PublicOfferingSummary;
import com.nifilili.offering.dto.response.PublicVariantResponse;
import com.nifilili.offering.service.PricingService;
import com.nifilili.offering.service.PublicOfferingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicOfferingControllerTest {

    private static final long OFFERING_ID = 1L;
    private static final long OWNER_ID = 100L;
    private static final long CATEGORY_ID = 10L;

    @Mock
    private PublicOfferingService service;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private PublicOfferingController controller;

    // ── get ──────────────────────────────────────────────────────────

    @Test
    void get_WhenPublished_ShouldReturnFullResponse() {
        OfferingEntity entity = buildEntity();
        List<PublicVariantResponse> variants = List.of(
                new PublicVariantResponse(50L, "SKU-001", BigDecimal.valueOf(29.99), Map.of("Color", "Red"))
        );

        when(service.getPublishedOffering(OFFERING_ID)).thenReturn(entity);
        when(pricingService.resolveOfferingPrice(OFFERING_ID)).thenReturn(BigDecimal.valueOf(89.99));
        when(service.getVariantsWithDetails(OFFERING_ID)).thenReturn(variants);

        PublicOfferingResponse result = controller.get(OFFERING_ID);

        assertEquals(OFFERING_ID, result.id());
        assertEquals("Test Offering", result.title());
        assertEquals(BigDecimal.valueOf(99.99), result.price());
        assertEquals(BigDecimal.valueOf(89.99), result.discountedPrice());
        assertEquals(1, result.variants().size());
        assertEquals("SKU-001", result.variants().get(0).sku());
    }

    @Test
    void get_WhenNotPublished_ShouldPropagateException() {
        when(service.getPublishedOffering(OFFERING_ID))
                .thenThrow(new IllegalArgumentException("Offering not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.get(OFFERING_ID));
    }

    // ── search ───────────────────────────────────────────────────────

    @Test
    void search_WhenFiltersProvided_ShouldPassToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PublicOfferingSummary> page = new PageImpl<>(List.of(
                new PublicOfferingSummary(1L, "Product", BigDecimal.TEN, true)
        ));

        when(service.searchOfferings(
                eq(CATEGORY_ID), eq(OfferingType.PRODUCT),
                eq(BigDecimal.ZERO), eq(BigDecimal.valueOf(500)),
                eq(pageable)
        )).thenReturn(page);

        Page<PublicOfferingSummary> result = controller.search(
                CATEGORY_ID, OfferingType.PRODUCT, BigDecimal.ZERO, BigDecimal.valueOf(500), pageable
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_WhenNoFilters_ShouldPassNulls() {
        Pageable pageable = PageRequest.of(0, 10);
        when(service.searchOfferings(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PublicOfferingSummary> result = controller.search(null, null, null, null, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── listByBusiness ───────────────────────────────────────────────

    @Test
    void listByBusiness_WhenCalled_ShouldDelegateToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PublicOfferingSummary> page = new PageImpl<>(List.of(
                new PublicOfferingSummary(1L, "Product", BigDecimal.TEN, false)
        ));
        when(service.listByOwner(OWNER_ID, pageable)).thenReturn(page);

        Page<PublicOfferingSummary> result = controller.listByBusiness(OWNER_ID, pageable);

        assertEquals(1, result.getTotalElements());
        verify(service).listByOwner(OWNER_ID, pageable);
    }

    // ── listByCategory ───────────────────────────────────────────────

    @Test
    void listByCategory_WhenCalled_ShouldDelegateToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PublicOfferingSummary> page = new PageImpl<>(List.of(
                new PublicOfferingSummary(1L, "Product", BigDecimal.TEN, false)
        ));
        when(service.listByCategory(CATEGORY_ID, pageable)).thenReturn(page);

        Page<PublicOfferingSummary> result = controller.listByCategory(CATEGORY_ID, pageable);

        assertEquals(1, result.getTotalElements());
        verify(service).listByCategory(CATEGORY_ID, pageable);
    }

    // ── featured ─────────────────────────────────────────────────────

    @Test
    void featured_WhenCalled_ShouldDelegateToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PublicOfferingSummary> page = new PageImpl<>(List.of(
                new PublicOfferingSummary(1L, "Featured", BigDecimal.TEN, true)
        ));
        when(service.listFeatured(pageable)).thenReturn(page);

        Page<PublicOfferingSummary> result = controller.featured(pageable);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).featured());
        verify(service).listFeatured(pageable);
    }

    @Test
    void featured_WhenEmpty_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(service.listFeatured(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<PublicOfferingSummary> result = controller.featured(pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingEntity buildEntity() {
        OfferingEntity entity = new OfferingEntity();
        TestEntityIdUtil.withId(entity, OFFERING_ID);
        entity.setOwnerId(OWNER_ID);
        entity.setTitle("Test Offering");
        entity.setDescription("Test Description");
        entity.setStatus(OfferingStatus.PUBLISHED);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setImages(List.of("img.jpg"));
        return entity;
    }
}
