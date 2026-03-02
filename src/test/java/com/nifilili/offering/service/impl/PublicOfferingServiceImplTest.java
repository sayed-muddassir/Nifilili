package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicOfferingServiceImplTest {

    private static final long OFFERING_ID = 1L;
    private static final long VARIANT_ID = 50L;
    private static final long OWNER_ID = 100L;
    private static final long CATEGORY_ID = 10L;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private OfferingVariantRepository variantRepository;

    @Mock
    private OfferingVariantAttributeRepository variantAttributeRepository;

    @InjectMocks
    private PublicOfferingServiceImpl publicOfferingService;

    // ── getPublishedOffering ─────────────────────────────────────────

    @Test
    void getPublishedOffering_WhenPublished_ShouldReturnEntity() {
        OfferingEntity entity = buildOfferingEntity();

        when(offeringRepository.findByIdAndStatus(OFFERING_ID, OfferingStatus.PUBLISHED))
                .thenReturn(Optional.of(entity));

        OfferingEntity result = publicOfferingService.getPublishedOffering(OFFERING_ID);

        assertEquals(OFFERING_ID, result.getId());
        assertEquals("Test Offering", result.getTitle());
    }

    @Test
    void getPublishedOffering_WhenNotPublished_ShouldThrow() {
        when(offeringRepository.findByIdAndStatus(OFFERING_ID, OfferingStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> publicOfferingService.getPublishedOffering(OFFERING_ID));
        assertEquals("Offering not found", ex.getMessage());
    }

    // ── getVariantsWithDetails ───────────────────────────────────────

    @Test
    void getVariantsWithDetails_WhenVariantsExist_ShouldReturnWithAttributes() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);
        variant.setSku("SKU-001");
        variant.setPrice(BigDecimal.valueOf(29.99));

        OfferingVariantAttributeEntity attr1 = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr1, 300L);
        attr1.setAttributeName("Color");
        attr1.setAttributeValue("Red");

        OfferingVariantAttributeEntity attr2 = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr2, 301L);
        attr2.setAttributeName("Size");
        attr2.setAttributeValue("Large");

        when(variantRepository.findByOfferingIdAndStatus(OFFERING_ID, "ACTIVE"))
                .thenReturn(List.of(variant));
        when(variantAttributeRepository.findByVariantId(VARIANT_ID))
                .thenReturn(List.of(attr1, attr2));

        List<PublicVariantResponse> result = publicOfferingService.getVariantsWithDetails(OFFERING_ID);

        assertEquals(1, result.size());
        PublicVariantResponse response = result.get(0);
        assertEquals(VARIANT_ID, response.id());
        assertEquals("SKU-001", response.sku());
        assertEquals(BigDecimal.valueOf(29.99), response.price());
        assertEquals("Red", response.attributes().get("Color"));
        assertEquals("Large", response.attributes().get("Size"));
    }

    @Test
    void getVariantsWithDetails_WhenNoVariants_ShouldReturnEmptyList() {
        when(variantRepository.findByOfferingIdAndStatus(OFFERING_ID, "ACTIVE"))
                .thenReturn(List.of());

        List<PublicVariantResponse> result = publicOfferingService.getVariantsWithDetails(OFFERING_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getVariantsWithDetails_WhenDuplicateAttributeNames_ShouldKeepFirst() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);
        variant.setSku("SKU-001");
        variant.setPrice(BigDecimal.valueOf(10));

        OfferingVariantAttributeEntity attr1 = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr1, 300L);
        attr1.setAttributeName("Color");
        attr1.setAttributeValue("Red");

        OfferingVariantAttributeEntity attr2 = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr2, 301L);
        attr2.setAttributeName("Color");
        attr2.setAttributeValue("Blue");

        when(variantRepository.findByOfferingIdAndStatus(OFFERING_ID, "ACTIVE"))
                .thenReturn(List.of(variant));
        when(variantAttributeRepository.findByVariantId(VARIANT_ID))
                .thenReturn(List.of(attr1, attr2));

        List<PublicVariantResponse> result = publicOfferingService.getVariantsWithDetails(OFFERING_ID);

        // merge function keeps existing (first) value
        assertEquals("Red", result.get(0).attributes().get("Color"));
    }

    // ── searchOfferings ──────────────────────────────────────────────

    @Test
    void searchOfferings_WhenMatchesExist_ShouldReturnSummaryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity();
        entity.setIsFeatured(true);
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.searchPublished(
                eq(CATEGORY_ID), eq(OfferingType.PRODUCT),
                eq(BigDecimal.ZERO), eq(BigDecimal.valueOf(500)),
                eq(pageable)
        )).thenReturn(page);

        Page<PublicOfferingSummary> result = publicOfferingService.searchOfferings(
                CATEGORY_ID, OfferingType.PRODUCT, BigDecimal.ZERO, BigDecimal.valueOf(500), pageable
        );

        assertEquals(1, result.getTotalElements());
        PublicOfferingSummary summary = result.getContent().get(0);
        assertEquals(OFFERING_ID, summary.id());
        assertEquals("Test Offering", summary.title());
        assertTrue(summary.featured());
    }

    @Test
    void searchOfferings_WhenNoFilters_ShouldPassNulls() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingEntity> emptyPage = new PageImpl<>(List.of());

        when(offeringRepository.searchPublished(null, null, null, null, pageable))
                .thenReturn(emptyPage);

        Page<PublicOfferingSummary> result = publicOfferingService.searchOfferings(
                null, null, null, null, pageable
        );

        assertEquals(0, result.getTotalElements());
        verify(offeringRepository).searchPublished(null, null, null, null, pageable);
    }

    // ── listByOwner ──────────────────────────────────────────────────

    @Test
    void listByOwner_WhenPublishedExist_ShouldReturnSummaryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity();
        entity.setIsFeatured(false);
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.findByOwnerIdAndStatus(OWNER_ID, OfferingStatus.PUBLISHED, pageable))
                .thenReturn(page);

        Page<PublicOfferingSummary> result = publicOfferingService.listByOwner(OWNER_ID, pageable);

        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).featured());
    }

    @Test
    void listByOwner_WhenNonePublished_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingEntity> emptyPage = new PageImpl<>(List.of());

        when(offeringRepository.findByOwnerIdAndStatus(OWNER_ID, OfferingStatus.PUBLISHED, pageable))
                .thenReturn(emptyPage);

        Page<PublicOfferingSummary> result = publicOfferingService.listByOwner(OWNER_ID, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── listByCategory ───────────────────────────────────────────────

    @Test
    void listByCategory_WhenPublishedExist_ShouldReturnSummaryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity();
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.findByCategoryIdAndStatus(CATEGORY_ID, OfferingStatus.PUBLISHED, pageable))
                .thenReturn(page);

        Page<PublicOfferingSummary> result = publicOfferingService.listByCategory(CATEGORY_ID, pageable);

        assertEquals(1, result.getTotalElements());
        verify(offeringRepository).findByCategoryIdAndStatus(CATEGORY_ID, OfferingStatus.PUBLISHED, pageable);
    }

    @Test
    void listByCategory_WhenEmpty_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingEntity> emptyPage = new PageImpl<>(List.of());

        when(offeringRepository.findByCategoryIdAndStatus(CATEGORY_ID, OfferingStatus.PUBLISHED, pageable))
                .thenReturn(emptyPage);

        Page<PublicOfferingSummary> result = publicOfferingService.listByCategory(CATEGORY_ID, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── listFeatured ─────────────────────────────────────────────────

    @Test
    void listFeatured_WhenFeaturedExist_ShouldReturnWithFeaturedTrue() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity();
        entity.setIsFeatured(true);
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.findByIsFeaturedTrueAndStatus(OfferingStatus.PUBLISHED, pageable))
                .thenReturn(page);

        Page<PublicOfferingSummary> result = publicOfferingService.listFeatured(pageable);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).featured());
    }

    @Test
    void listFeatured_WhenNone_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingEntity> emptyPage = new PageImpl<>(List.of());

        when(offeringRepository.findByIsFeaturedTrueAndStatus(OfferingStatus.PUBLISHED, pageable))
                .thenReturn(emptyPage);

        Page<PublicOfferingSummary> result = publicOfferingService.listFeatured(pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingEntity buildOfferingEntity() {
        OfferingEntity entity = new OfferingEntity();
        TestEntityIdUtil.withId(entity, OFFERING_ID);
        entity.setOwnerId(OWNER_ID);
        entity.setTitle("Test Offering");
        entity.setDescription("Test Description");
        entity.setStatus(OfferingStatus.PUBLISHED);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setImages(List.of("img.jpg"));
        entity.setIsFeatured(false);
        entity.setIsB2bEnabled(false);
        entity.setViewCount(0);
        return entity;
    }
}
