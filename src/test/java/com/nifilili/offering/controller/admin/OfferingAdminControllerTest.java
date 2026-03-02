package com.nifilili.offering.controller.admin;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import com.nifilili.offering.dto.request.AdminStatusChangeRequest;
import com.nifilili.offering.dto.response.OfferingResponse;
import com.nifilili.offering.service.OfferingAdminService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingAdminControllerTest {

    private static final long OFFERING_ID = 1L;

    @Mock
    private OfferingAdminService offeringAdminService;

    @InjectMocks
    private OfferingAdminController controller;

    // ── listAll ──────────────────────────────────────────────────────

    @Test
    void listAll_WhenStatusProvided_ShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OfferingEntity entity = buildEntity(OfferingStatus.PUBLISHED);
        Page<OfferingEntity> page = new PageImpl<>(List.of(entity));

        when(offeringAdminService.listAll(OfferingStatus.PUBLISHED, pageable)).thenReturn(page);

        Page<OfferingResponse> result = controller.listAll(OfferingStatus.PUBLISHED, pageable);

        assertEquals(1, result.getTotalElements());
        OfferingResponse dto = result.getContent().get(0);
        assertEquals(OFFERING_ID, dto.id());
        assertEquals("Test Offering", dto.title());
        assertEquals(OfferingStatus.PUBLISHED, dto.status());
    }

    @Test
    void listAll_WhenNullStatus_ShouldPassNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(offeringAdminService.listAll(null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<OfferingResponse> result = controller.listAll(null, pageable);

        assertTrue(result.getContent().isEmpty());
        verify(offeringAdminService).listAll(null, pageable);
    }

    // ── changeStatus ─────────────────────────────────────────────────

    @Test
    void changeStatus_WhenValid_ShouldReturnUpdatedResponse() {
        AdminStatusChangeRequest request = new AdminStatusChangeRequest(OfferingStatus.ARCHIVED);
        OfferingEntity updated = buildEntity(OfferingStatus.ARCHIVED);

        when(offeringAdminService.changeStatus(OFFERING_ID, OfferingStatus.ARCHIVED)).thenReturn(updated);

        OfferingResponse result = controller.changeStatus(OFFERING_ID, request);

        assertEquals(OFFERING_ID, result.id());
        assertEquals(OfferingStatus.ARCHIVED, result.status());
    }

    @Test
    void changeStatus_WhenNotFound_ShouldPropagateException() {
        AdminStatusChangeRequest request = new AdminStatusChangeRequest(OfferingStatus.ARCHIVED);
        when(offeringAdminService.changeStatus(OFFERING_ID, OfferingStatus.ARCHIVED))
                .thenThrow(new IllegalArgumentException("Offering not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.changeStatus(OFFERING_ID, request));
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingEntity buildEntity(OfferingStatus status) {
        OfferingEntity entity = new OfferingEntity();
        TestEntityIdUtil.withId(entity, OFFERING_ID);
        entity.setTitle("Test Offering");
        entity.setDescription("Test Description");
        entity.setStatus(status);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setImages(List.of("img.jpg"));
        return entity;
    }
}
