package com.nifilili.offering.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingVariantServiceImplTest {

    private static final long USER_ID = 100L;
    private static final long OFFERING_ID = 1L;
    private static final long VARIANT_ID = 50L;

    @Mock
    private OfferingVariantRepository variantRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @InjectMocks
    private OfferingVariantServiceImpl variantService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenOfferingExists_ShouldSetDefaultsAndSave() {
        OfferingEntity offering = new OfferingEntity();
        TestEntityIdUtil.withId(offering, OFFERING_ID);

        OfferingVariantEntity variant = new OfferingVariantEntity();
        variant.setSku("SKU-001");
        variant.setPrice(BigDecimal.valueOf(29.99));

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(variantRepository.save(any(OfferingVariantEntity.class))).thenAnswer(inv -> {
            OfferingVariantEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, VARIANT_ID);
            return saved;
        });

        OfferingVariantEntity result = variantService.create(OFFERING_ID, variant);

        assertEquals(offering, result.getOffering());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(USER_ID, result.getCreatedBy());
        assertEquals(USER_ID, result.getUpdatedBy());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        ArgumentCaptor<OfferingVariantEntity> captor = ArgumentCaptor.forClass(OfferingVariantEntity.class);
        verify(variantRepository).save(captor.capture());
        assertEquals("SKU-001", captor.getValue().getSku());
    }

    @Test
    void create_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        OfferingVariantEntity variant = new OfferingVariantEntity();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantService.create(OFFERING_ID, variant));
        assertEquals("Offering not found", ex.getMessage());
        verify(variantRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenVariantExists_ShouldUpdateFields() {
        OfferingVariantEntity existing = buildVariantEntity();

        OfferingVariantEntity updates = new OfferingVariantEntity();
        updates.setSku("SKU-UPDATED");
        updates.setPrice(BigDecimal.valueOf(39.99));
        updates.setAvailableQuantity(200L);
        updates.setImages(List.of("new-img.jpg"));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(existing));
        when(variantRepository.save(any(OfferingVariantEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferingVariantEntity result = variantService.update(VARIANT_ID, updates);

        assertEquals("SKU-UPDATED", result.getSku());
        assertEquals(BigDecimal.valueOf(39.99), result.getPrice());
        assertEquals(200L, result.getAvailableQuantity());
        assertEquals(List.of("new-img.jpg"), result.getImages());
        assertEquals(USER_ID, result.getUpdatedBy());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void update_WhenVariantNotFound_ShouldThrow() {
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.empty());

        OfferingVariantEntity updates = new OfferingVariantEntity();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantService.update(VARIANT_ID, updates));
        assertEquals("Variant not found", ex.getMessage());
        verify(variantRepository, never()).save(any());
    }

    // ── list ─────────────────────────────────────────────────────────

    @Test
    void list_WhenVariantsExist_ShouldReturnAll() {
        OfferingVariantEntity v1 = buildVariantEntity();
        OfferingVariantEntity v2 = new OfferingVariantEntity();
        TestEntityIdUtil.withId(v2, 51L);
        v2.setSku("SKU-002");

        when(variantRepository.findByOfferingId(OFFERING_ID)).thenReturn(List.of(v1, v2));

        List<OfferingVariantEntity> result = variantService.list(OFFERING_ID);

        assertEquals(2, result.size());
        verify(variantRepository).findByOfferingId(OFFERING_ID);
    }

    @Test
    void list_WhenNoVariants_ShouldReturnEmptyList() {
        when(variantRepository.findByOfferingId(OFFERING_ID)).thenReturn(List.of());

        List<OfferingVariantEntity> result = variantService.list(OFFERING_ID);

        assertTrue(result.isEmpty());
    }

    // ── listPaginated ────────────────────────────────────────────────

    @Test
    void listPaginated_WhenCalled_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingVariantEntity variant = buildVariantEntity();
        Page<OfferingVariantEntity> page = new PageImpl<>(List.of(variant));

        when(variantRepository.findByOfferingId(OFFERING_ID, pageable)).thenReturn(page);

        Page<OfferingVariantEntity> result = variantService.listPaginated(OFFERING_ID, pageable);

        assertEquals(1, result.getTotalElements());
        verify(variantRepository).findByOfferingId(OFFERING_ID, pageable);
    }

    // ── updateInventory ──────────────────────────────────────────────

    @Test
    void updateInventory_WhenCalled_ShouldDelegateToRepository() {
        variantService.updateInventory(VARIANT_ID, 500L);

        verify(variantRepository).updateInventory(VARIANT_ID, 500L);
    }

    // ── deactivate ───────────────────────────────────────────────────

    @Test
    void deactivate_WhenVariantExists_ShouldSetStatusToInactive() {
        OfferingVariantEntity variant = buildVariantEntity();
        variant.setStatus("ACTIVE");

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(variantRepository.save(any(OfferingVariantEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        variantService.deactivate(VARIANT_ID);

        assertEquals("INACTIVE", variant.getStatus());
        assertEquals(USER_ID, variant.getUpdatedBy());
        assertNotNull(variant.getUpdatedAt());
        verify(variantRepository).save(variant);
    }

    @Test
    void deactivate_WhenVariantNotFound_ShouldThrow() {
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantService.deactivate(VARIANT_ID));
        assertEquals("Variant not found", ex.getMessage());
        verify(variantRepository, never()).save(any());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingVariantEntity buildVariantEntity() {
        OfferingVariantEntity entity = new OfferingVariantEntity();
        TestEntityIdUtil.withId(entity, VARIANT_ID);
        entity.setSku("SKU-001");
        entity.setPrice(BigDecimal.valueOf(29.99));
        entity.setAvailableQuantity(100L);
        entity.setImages(List.of("img.jpg"));
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(USER_ID);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(USER_ID);
        return entity;
    }
}
