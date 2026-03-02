package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.offering.domain.OfferingDiscountEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.CreateDiscountRequest;
import com.nifilili.offering.dto.response.DiscountDetailResponse;
import com.nifilili.offering.repository.OfferingDiscountRepository;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    private static final long OFFERING_ID = 1L;
    private static final long VARIANT_ID = 50L;
    private static final long DISCOUNT_ID = 200L;

    @Mock
    private OfferingDiscountRepository discountRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private OfferingVariantRepository variantRepository;

    @InjectMocks
    private DiscountServiceImpl discountService;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenOfferingOnlyDiscount_ShouldSaveWithoutVariant() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(30);

        CreateDiscountRequest request = new CreateDiscountRequest(
                OFFERING_ID, null, DiscountType.PERCENTAGE, 10L, start, end
        );

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(discountRepository.save(any(OfferingDiscountEntity.class))).thenAnswer(inv -> {
            OfferingDiscountEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, DISCOUNT_ID);
            return saved;
        });

        OfferingDiscountEntity result = discountService.create(request);

        assertEquals(offering, result.getOffering());
        assertNull(result.getVariant());
        assertEquals(DiscountType.PERCENTAGE, result.getDiscountType());
        assertEquals(10L, result.getDiscountValue());
        assertEquals("ACTIVE", result.getStatus());

        ArgumentCaptor<OfferingDiscountEntity> captor = ArgumentCaptor.forClass(OfferingDiscountEntity.class);
        verify(discountRepository).save(captor.capture());
        assertEquals(start, captor.getValue().getStartDate());
        assertEquals(end, captor.getValue().getEndDate());
    }

    @Test
    void create_WhenVariantDiscount_ShouldSaveWithVariant() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);

        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);

        CreateDiscountRequest request = new CreateDiscountRequest(
                OFFERING_ID, VARIANT_ID, DiscountType.FIXED_AMOUNT, 500L, start, end
        );

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(discountRepository.save(any(OfferingDiscountEntity.class))).thenAnswer(inv -> {
            OfferingDiscountEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, DISCOUNT_ID);
            return saved;
        });

        OfferingDiscountEntity result = discountService.create(request);

        assertEquals(offering, result.getOffering());
        assertEquals(variant, result.getVariant());
        assertEquals(DiscountType.FIXED_AMOUNT, result.getDiscountType());
        assertEquals(500L, result.getDiscountValue());
    }

    @Test
    void create_WhenOfferingNotFound_ShouldThrow() {
        CreateDiscountRequest request = new CreateDiscountRequest(
                OFFERING_ID, null, DiscountType.PERCENTAGE, 10L,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> discountService.create(request));
        assertEquals("Offering not found", ex.getMessage());
        verify(discountRepository, never()).save(any());
    }

    @Test
    void create_WhenVariantNotFound_ShouldThrow() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);

        CreateDiscountRequest request = new CreateDiscountRequest(
                OFFERING_ID, VARIANT_ID, DiscountType.PERCENTAGE, 10L,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> discountService.create(request));
        assertEquals("Variant not found", ex.getMessage());
        verify(discountRepository, never()).save(any());
    }

    // ── listByOffering ───────────────────────────────────────────────

    @Test
    void listByOffering_WhenDiscountsExist_ShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);

        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);

        OfferingDiscountEntity discount = buildDiscountEntity(offering, null);

        Page<OfferingDiscountEntity> page = new PageImpl<>(List.of(discount));
        when(discountRepository.findByOfferingId(OFFERING_ID, pageable)).thenReturn(page);

        Page<DiscountDetailResponse> result = discountService.listByOffering(OFFERING_ID, pageable);

        assertEquals(1, result.getTotalElements());
        DiscountDetailResponse dto = result.getContent().get(0);
        assertEquals(DISCOUNT_ID, dto.id());
        assertEquals(OFFERING_ID, dto.offeringId());
        assertNull(dto.variantId());
        assertEquals(DiscountType.PERCENTAGE, dto.discountType());
        assertEquals(10L, dto.discountValue());
        assertEquals("ACTIVE", dto.status());
    }

    @Test
    void listByOffering_WhenDiscountHasVariant_ShouldIncludeVariantId() {
        Pageable pageable = PageRequest.of(0, 10);

        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);

        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingDiscountEntity discount = buildDiscountEntity(offering, variant);

        Page<OfferingDiscountEntity> page = new PageImpl<>(List.of(discount));
        when(discountRepository.findByOfferingId(OFFERING_ID, pageable)).thenReturn(page);

        Page<DiscountDetailResponse> result = discountService.listByOffering(OFFERING_ID, pageable);

        assertEquals(VARIANT_ID, result.getContent().get(0).variantId());
    }

    @Test
    void listByOffering_WhenEmpty_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingDiscountEntity> emptyPage = new PageImpl<>(List.of());

        when(discountRepository.findByOfferingId(OFFERING_ID, pageable)).thenReturn(emptyPage);

        Page<DiscountDetailResponse> result = discountService.listByOffering(OFFERING_ID, pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ── deactivate ───────────────────────────────────────────────────

    @Test
    void deactivate_WhenDiscountExists_ShouldSetStatusToInactive() {
        OfferingDiscountEntity discount = new OfferingDiscountEntity();
        TestEntityIdUtil.withId(discount, DISCOUNT_ID);
        discount.setStatus("ACTIVE");

        when(discountRepository.findById(DISCOUNT_ID)).thenReturn(Optional.of(discount));
        when(discountRepository.save(any(OfferingDiscountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        discountService.deactivate(DISCOUNT_ID);

        assertEquals("INACTIVE", discount.getStatus());
        verify(discountRepository).save(discount);
    }

    @Test
    void deactivate_WhenDiscountNotFound_ShouldThrow() {
        when(discountRepository.findById(DISCOUNT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> discountService.deactivate(DISCOUNT_ID));
        assertEquals("Discount not found", ex.getMessage());
        verify(discountRepository, never()).save(any());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingDiscountEntity buildDiscountEntity(OfferingEntity offering, OfferingVariantEntity variant) {
        OfferingDiscountEntity entity = new OfferingDiscountEntity();
        TestEntityIdUtil.withId(entity, DISCOUNT_ID);
        entity.setOffering(offering);
        entity.setVariant(variant);
        entity.setDiscountType(DiscountType.PERCENTAGE);
        entity.setDiscountValue(10L);
        entity.setStartDate(LocalDateTime.now());
        entity.setEndDate(LocalDateTime.now().plusDays(30));
        entity.setStatus("ACTIVE");
        return entity;
    }
}
