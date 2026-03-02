package com.nifilili.offering.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.repository.OfferingRepository;
import com.nifilili.offering.service.OfferingCategoryService;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingServiceImplTest {

    private static final long USER_ID = 100L;
    private static final long OFFERING_ID = 1L;
    private static final long CATEGORY_ID = 10L;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private OfferingCategoryService categoryService;

    @InjectMocks
    private OfferingServiceImpl offeringService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ── listMyOfferings ──────────────────────────────────────────────

    @Test
    void listMyOfferings_WhenStatusIsNull_ShouldReturnAllOwnerOfferings() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity();
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.findByOwnerId(USER_ID, pageable)).thenReturn(page);

        Page<OfferingResponse> result = offeringService.listMyOfferings(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(entity.getTitle(), result.getContent().get(0).title());
        verify(offeringRepository).findByOwnerId(USER_ID, pageable);
        verify(offeringRepository, never()).findByOwnerIdAndStatus(anyLong(), any(), any());
    }

    @Test
    void listMyOfferings_WhenStatusProvided_ShouldReturnFilteredOfferings() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity();
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.findByOwnerIdAndStatus(USER_ID, OfferingStatus.PUBLISHED, pageable))
                .thenReturn(page);

        Page<OfferingResponse> result = offeringService.listMyOfferings(OfferingStatus.PUBLISHED, pageable);

        assertEquals(1, result.getTotalElements());
        verify(offeringRepository).findByOwnerIdAndStatus(USER_ID, OfferingStatus.PUBLISHED, pageable);
        verify(offeringRepository, never()).findByOwnerId(anyLong(), any());
    }

    @Test
    void listMyOfferings_WhenNoOfferings_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingEntity> emptyPage = new PageImpl<>(List.of());

        when(offeringRepository.findByOwnerId(USER_ID, pageable)).thenReturn(emptyPage);

        Page<OfferingResponse> result = offeringService.listMyOfferings(null, pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenValidInput_ShouldSetDefaultsAndSave() {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);
        category.setName("Electronics");

        when(categoryService.validateLeafCategory(CATEGORY_ID)).thenReturn(category);

        OfferingEntity offering = new OfferingEntity();
        offering.setTitle("Test Product");
        offering.setDescription("Description");
        offering.setPrice(BigDecimal.valueOf(99.99));

        when(offeringRepository.save(any(OfferingEntity.class))).thenAnswer(inv -> {
            OfferingEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, OFFERING_ID);
            return saved;
        });

        OfferingEntity result = offeringService.create(offering, CATEGORY_ID);

        assertEquals(OfferingStatus.DRAFT, result.getStatus());
        assertEquals(0, result.getViewCount());
        assertEquals(USER_ID, result.getCreatedBy());
        assertEquals(USER_ID, result.getUpdatedBy());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(category, result.getCategory());

        ArgumentCaptor<OfferingEntity> captor = ArgumentCaptor.forClass(OfferingEntity.class);
        verify(offeringRepository).save(captor.capture());
        assertEquals("Test Product", captor.getValue().getTitle());
    }

    @Test
    void create_WhenCategoryNotLeaf_ShouldThrowFromCategoryService() {
        when(categoryService.validateLeafCategory(CATEGORY_ID))
                .thenThrow(new IllegalStateException("Offering must be assigned to a leaf category"));

        OfferingEntity offering = new OfferingEntity();

        assertThrows(IllegalStateException.class,
                () -> offeringService.create(offering, CATEGORY_ID));
        verify(offeringRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenOfferingExists_ShouldUpdateFields() {
        OfferingEntity existing = buildOfferingEntity();
        existing.setStatus(OfferingStatus.DRAFT);

        OfferingEntity updates = new OfferingEntity();
        updates.setTitle("Updated Title");
        updates.setDescription("Updated Desc");
        updates.setPrice(BigDecimal.valueOf(149.99));
        updates.setAvailableQuantity(50);
        updates.setIsFeatured(true);
        updates.setIsB2bEnabled(false);
        updates.setImages(List.of("new-img.jpg"));

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(existing));
        when(offeringRepository.save(any(OfferingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferingEntity result = offeringService.update(OFFERING_ID, updates);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals(BigDecimal.valueOf(149.99), result.getPrice());
        assertEquals(50, result.getAvailableQuantity());
        assertTrue(result.getIsFeatured());
        assertFalse(result.getIsB2bEnabled());
        assertEquals(List.of("new-img.jpg"), result.getImages());
        assertEquals(USER_ID, result.getUpdatedBy());
    }

    @Test
    void update_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        OfferingEntity updates = new OfferingEntity();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> offeringService.update(OFFERING_ID, updates));
        assertEquals("Offering not found", ex.getMessage());
        verify(offeringRepository, never()).save(any());
    }

    @Test
    void update_WhenOfferingIsArchived_ShouldThrow() {
        OfferingEntity existing = buildOfferingEntity();
        existing.setStatus(OfferingStatus.ARCHIVED);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(existing));

        OfferingEntity updates = new OfferingEntity();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> offeringService.update(OFFERING_ID, updates));
        assertEquals("Archived offering cannot be modified", ex.getMessage());
        verify(offeringRepository, never()).save(any());
    }

    // ── getMyOffering ────────────────────────────────────────────────

    @Test
    void getMyOffering_WhenOwnerMatches_ShouldReturnOffering() {
        OfferingEntity entity = buildOfferingEntity();

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));

        OfferingEntity result = offeringService.getMyOffering(OFFERING_ID);

        assertEquals(OFFERING_ID, result.getId());
        assertEquals(USER_ID, result.getOwnerId());
    }

    @Test
    void getMyOffering_WhenOwnerDoesNotMatch_ShouldThrow() {
        OfferingEntity entity = buildOfferingEntity();
        entity.setOwnerId(999L); // different owner

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> offeringService.getMyOffering(OFFERING_ID));
        assertEquals("Offering not found", ex.getMessage());
    }

    @Test
    void getMyOffering_WhenNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> offeringService.getMyOffering(OFFERING_ID));
    }

    // ── publish ──────────────────────────────────────────────────────

    @Test
    void publish_WhenOfferingExists_ShouldSetStatusToPublished() {
        OfferingEntity entity = buildOfferingEntity();
        entity.setStatus(OfferingStatus.DRAFT);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));

        offeringService.publish(OFFERING_ID);

        assertEquals(OfferingStatus.PUBLISHED, entity.getStatus());
    }

    @Test
    void publish_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> offeringService.publish(OFFERING_ID));
    }

    // ── archive ──────────────────────────────────────────────────────

    @Test
    void archive_WhenOfferingExists_ShouldSetStatusToArchived() {
        OfferingEntity entity = buildOfferingEntity();
        entity.setStatus(OfferingStatus.PUBLISHED);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));

        offeringService.archive(OFFERING_ID);

        assertEquals(OfferingStatus.ARCHIVED, entity.getStatus());
    }

    @Test
    void archive_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> offeringService.archive(OFFERING_ID));
    }

    // ── restore ──────────────────────────────────────────────────────

    @Test
    void restore_WhenOfferingExists_ShouldSetStatusToDraft() {
        OfferingEntity entity = buildOfferingEntity();
        entity.setStatus(OfferingStatus.ARCHIVED);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));

        offeringService.restore(OFFERING_ID);

        assertEquals(OfferingStatus.DRAFT, entity.getStatus());
    }

    @Test
    void restore_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> offeringService.restore(OFFERING_ID));
    }

    // ── updateInventory ──────────────────────────────────────────────

    @Test
    void updateInventory_WhenCalled_ShouldDelegateToRepository() {
        offeringService.updateInventory(OFFERING_ID, 100);

        verify(offeringRepository).updateInventory(OFFERING_ID, 100);
    }

    // ── get ──────────────────────────────────────────────────────────

    @Test
    void get_WhenOfferingExists_ShouldReturnEntity() {
        OfferingEntity entity = buildOfferingEntity();

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));

        OfferingEntity result = offeringService.get(OFFERING_ID);

        assertEquals(OFFERING_ID, result.getId());
    }

    @Test
    void get_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> offeringService.get(OFFERING_ID));
        assertEquals("Offering not found", ex.getMessage());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingEntity buildOfferingEntity() {
        OfferingEntity entity = new OfferingEntity();
        TestEntityIdUtil.withId(entity, OFFERING_ID);
        entity.setOwnerId(USER_ID);
        entity.setTitle("Test Offering");
        entity.setDescription("Test Description");
        entity.setStatus(OfferingStatus.DRAFT);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setImages(List.of("img1.jpg"));
        entity.setIsFeatured(false);
        entity.setIsB2bEnabled(false);
        entity.setViewCount(0);
        return entity;
    }
}
