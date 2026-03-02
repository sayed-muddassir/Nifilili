package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.repository.OfferingRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingAdminServiceImplTest {

    private static final long OFFERING_ID = 1L;

    @Mock
    private OfferingRepository offeringRepository;

    @InjectMocks
    private OfferingAdminServiceImpl adminService;

    // ── listAll ──────────────────────────────────────────────────────

    @Test
    void listAll_WhenStatusProvided_ShouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildOfferingEntity(OfferingStatus.PUBLISHED);
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringRepository.findByStatus(OfferingStatus.PUBLISHED, pageable)).thenReturn(page);

        Page<OfferingEntity> result = adminService.listAll(OfferingStatus.PUBLISHED, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(OfferingStatus.PUBLISHED, result.getContent().get(0).getStatus());
        verify(offeringRepository).findByStatus(OfferingStatus.PUBLISHED, pageable);
        verify(offeringRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listAll_WhenStatusIsNull_ShouldReturnAllOfferings() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity e1 = buildOfferingEntity(OfferingStatus.DRAFT);
        OfferingEntity e2 = buildOfferingEntity(OfferingStatus.PUBLISHED);
        TestEntityIdUtil.withId(e2, 2L);
        Page<OfferingEntity> page = new PageImpl<>(List.of(e1, e2));

        when(offeringRepository.findAll(pageable)).thenReturn(page);

        Page<OfferingEntity> result = adminService.listAll(null, pageable);

        assertEquals(2, result.getTotalElements());
        verify(offeringRepository).findAll(pageable);
        verify(offeringRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listAll_WhenEmpty_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingEntity> emptyPage = new PageImpl<>(List.of());

        when(offeringRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<OfferingEntity> result = adminService.listAll(null, pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ── changeStatus ─────────────────────────────────────────────────

    @Test
    void changeStatus_WhenOfferingExists_ShouldUpdateStatus() {
        OfferingEntity entity = buildOfferingEntity(OfferingStatus.PUBLISHED);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));
        when(offeringRepository.save(any(OfferingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferingEntity result = adminService.changeStatus(OFFERING_ID, OfferingStatus.ARCHIVED);

        assertEquals(OfferingStatus.ARCHIVED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        ArgumentCaptor<OfferingEntity> captor = ArgumentCaptor.forClass(OfferingEntity.class);
        verify(offeringRepository).save(captor.capture());
        assertEquals(OfferingStatus.ARCHIVED, captor.getValue().getStatus());
    }

    @Test
    void changeStatus_WhenOfferingNotFound_ShouldThrow() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.changeStatus(OFFERING_ID, OfferingStatus.ARCHIVED));
        assertEquals("Offering not found", ex.getMessage());
        verify(offeringRepository, never()).save(any());
    }

    @Test
    void changeStatus_WhenChangingToDraft_ShouldSetDraftStatus() {
        OfferingEntity entity = buildOfferingEntity(OfferingStatus.ARCHIVED);

        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(entity));
        when(offeringRepository.save(any(OfferingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferingEntity result = adminService.changeStatus(OFFERING_ID, OfferingStatus.DRAFT);

        assertEquals(OfferingStatus.DRAFT, result.getStatus());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingEntity buildOfferingEntity(OfferingStatus status) {
        OfferingEntity entity = new OfferingEntity();
        TestEntityIdUtil.withId(entity, OFFERING_ID);
        entity.setOwnerId(100L);
        entity.setTitle("Test Offering");
        entity.setDescription("Test Description");
        entity.setStatus(status);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setIsFeatured(false);
        entity.setIsB2bEnabled(false);
        entity.setViewCount(0);
        return entity;
    }
}
