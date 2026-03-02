package com.nifilili.offering.controller.owner;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.offering.OfferingOwnerType;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.core.enums.offering.OfferingType;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.request.CreateOfferingRequest;
import com.nifilili.offering.dto.request.UpdateOfferingRequest;
import com.nifilili.offering.dto.response.CreateOfferingResponse;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.OfferingService;
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
class OfferingControllerTest {

    private static final long OFFERING_ID = 1L;
    private static final long OWNER_ID = 100L;

    @Mock
    private OfferingService offeringService;

    @InjectMocks
    private OfferingController controller;

    // ── listMyOfferings ──────────────────────────────────────────────

    @Test
    void listMyOfferings_WhenCalled_ShouldDelegateToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingResponse> page = new PageImpl<>(List.of(
                new OfferingResponse(1L, "Test", "Desc", OfferingStatus.DRAFT, BigDecimal.TEN, List.of())
        ));
        when(offeringService.listMyOfferings(OfferingStatus.DRAFT, pageable)).thenReturn(page);

        Page<OfferingResponse> result = controller.listMyOfferings(OfferingStatus.DRAFT, pageable);

        assertEquals(1, result.getTotalElements());
        verify(offeringService).listMyOfferings(OfferingStatus.DRAFT, pageable);
    }

    @Test
    void listMyOfferings_WhenNullStatus_ShouldPassNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OfferingResponse> emptyPage = new PageImpl<>(List.of());
        when(offeringService.listMyOfferings(null, pageable)).thenReturn(emptyPage);

        Page<OfferingResponse> result = controller.listMyOfferings(null, pageable);

        assertTrue(result.getContent().isEmpty());
        verify(offeringService).listMyOfferings(null, pageable);
    }

    // ── getMyOffering ────────────────────────────────────────────────

    @Test
    void getMyOffering_WhenFound_ShouldMapToResponse() {
        OfferingEntity entity = buildEntity();
        when(offeringService.getMyOffering(OFFERING_ID)).thenReturn(entity);

        OfferingResponse result = controller.getMyOffering(OFFERING_ID);

        assertEquals(OFFERING_ID, result.id());
        assertEquals("Test Offering", result.title());
        assertEquals(OfferingStatus.DRAFT, result.status());
    }

    @Test
    void getMyOffering_WhenNotFound_ShouldPropagateException() {
        when(offeringService.getMyOffering(OFFERING_ID))
                .thenThrow(new IllegalArgumentException("Offering not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.getMyOffering(OFFERING_ID));
    }

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenValidRequest_ShouldMapRequestAndReturnResponse() {
        CreateOfferingRequest request = new CreateOfferingRequest(
                OfferingOwnerType.BUSINESS, OWNER_ID, 10L, OfferingType.PRODUCT,
                "New Product", "Description", "SKU-001", BigDecimal.valueOf(99.99),
                false, 50, false, false, List.of("img.jpg")
        );

        OfferingEntity saved = buildEntity();
        saved.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(offeringService.create(any(OfferingEntity.class), eq(10L))).thenReturn(saved);

        CreateOfferingResponse result = controller.create(request);

        assertEquals(OFFERING_ID, result.id());
        assertEquals(OfferingStatus.DRAFT, result.status());
        assertNotNull(result.createdAt());

        ArgumentCaptor<OfferingEntity> captor = ArgumentCaptor.forClass(OfferingEntity.class);
        verify(offeringService).create(captor.capture(), eq(10L));
        assertEquals("New Product", captor.getValue().getTitle());
        assertEquals(OfferingOwnerType.BUSINESS, captor.getValue().getOwnerType());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenValidRequest_ShouldMapAndReturnUpdated() {
        UpdateOfferingRequest request = new UpdateOfferingRequest(
                "Updated Title", "Updated Desc", BigDecimal.valueOf(149.99),
                100, true, false, List.of("new.jpg")
        );

        OfferingEntity updated = buildEntity();
        updated.setTitle("Updated Title");
        updated.setPrice(BigDecimal.valueOf(149.99));
        when(offeringService.update(eq(OFFERING_ID), any(OfferingEntity.class))).thenReturn(updated);

        OfferingResponse result = controller.update(OFFERING_ID, request);

        assertEquals("Updated Title", result.title());
        verify(offeringService).update(eq(OFFERING_ID), any(OfferingEntity.class));
    }

    @Test
    void update_WhenNotFound_ShouldPropagateException() {
        UpdateOfferingRequest request = new UpdateOfferingRequest(
                "Title", "Desc", BigDecimal.TEN, 10, false, false, List.of()
        );
        when(offeringService.update(eq(OFFERING_ID), any(OfferingEntity.class)))
                .thenThrow(new IllegalArgumentException("Offering not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(OFFERING_ID, request));
    }

    // ── publish ──────────────────────────────────────────────────────

    @Test
    void publish_WhenCalled_ShouldReturnPublishedStatus() {
        StatusResponse result = controller.publish(OFFERING_ID);

        assertEquals("PUBLISHED", result.status());
        verify(offeringService).publish(OFFERING_ID);
    }

    // ── archive ──────────────────────────────────────────────────────

    @Test
    void archive_WhenCalled_ShouldReturnArchivedStatus() {
        StatusResponse result = controller.archive(OFFERING_ID);

        assertEquals("ARCHIVED", result.status());
        verify(offeringService).archive(OFFERING_ID);
    }

    // ── restore ──────────────────────────────────────────────────────

    @Test
    void restore_WhenCalled_ShouldReturnDraftStatus() {
        StatusResponse result = controller.restore(OFFERING_ID);

        assertEquals("DRAFT", result.status());
        verify(offeringService).restore(OFFERING_ID);
    }

    // ── inventory ────────────────────────────────────────────────────

    @Test
    void inventory_WhenCalled_ShouldDelegateAndReturnUpdated() {
        StatusResponse result = controller.inventory(OFFERING_ID, 200);

        assertEquals("UPDATED", result.status());
        verify(offeringService).updateInventory(OFFERING_ID, 200);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingEntity buildEntity() {
        OfferingEntity entity = new OfferingEntity();
        TestEntityIdUtil.withId(entity, OFFERING_ID);
        entity.setOwnerId(OWNER_ID);
        entity.setTitle("Test Offering");
        entity.setDescription("Test Description");
        entity.setStatus(OfferingStatus.DRAFT);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setImages(List.of("img.jpg"));
        return entity;
    }
}
