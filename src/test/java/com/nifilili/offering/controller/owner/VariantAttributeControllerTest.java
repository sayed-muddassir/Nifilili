package com.nifilili.offering.controller.owner;

import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.dto.request.UpdateVariantAttributeRequest;
import com.nifilili.offering.dto.request.VariantAttributeItem;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.dto.response.VariantAttributeDetailResponse;
import com.nifilili.offering.dto.response.VariantAttributeResponse;
import com.nifilili.offering.service.VariantAttributeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariantAttributeControllerTest {

    private static final long VARIANT_ID = 50L;
    private static final long ATTRIBUTE_ID = 300L;

    @Mock
    private VariantAttributeService service;

    @InjectMocks
    private VariantAttributeController controller;

    // ── assign ───────────────────────────────────────────────────────

    @Test
    void assign_WhenValid_ShouldReturnSavedResponse() {
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(
                List.of(new VariantAttributeItem(20L, "Color", "Red"))
        );

        VariantAttributeResponse result = controller.assign(VARIANT_ID, request);

        assertEquals(VARIANT_ID, result.variantId());
        assertTrue(result.saved());
        verify(service).assignAttributes(VARIANT_ID, request);
    }

    @Test
    void assign_WhenVariantNotFound_ShouldPropagateException() {
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of());
        doThrow(new IllegalArgumentException("Variant not found"))
                .when(service).assignAttributes(eq(VARIANT_ID), any());

        assertThrows(IllegalArgumentException.class,
                () -> controller.assign(VARIANT_ID, request));
    }

    // ── list ─────────────────────────────────────────────────────────

    @Test
    void list_WhenAttributesExist_ShouldReturnAll() {
        List<VariantAttributeDetailResponse> attrs = List.of(
                new VariantAttributeDetailResponse(300L, 20L, "Color", "Red"),
                new VariantAttributeDetailResponse(301L, null, "Custom", "Value")
        );
        when(service.listAttributes(VARIANT_ID)).thenReturn(attrs);

        List<VariantAttributeDetailResponse> result = controller.list(VARIANT_ID);

        assertEquals(2, result.size());
        assertEquals("Color", result.get(0).attributeName());
        assertNull(result.get(1).offeringAttributeId());
    }

    @Test
    void list_WhenEmpty_ShouldReturnEmptyList() {
        when(service.listAttributes(VARIANT_ID)).thenReturn(List.of());

        List<VariantAttributeDetailResponse> result = controller.list(VARIANT_ID);

        assertTrue(result.isEmpty());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenValid_ShouldReturnUpdatedStatus() {
        UpdateVariantAttributeRequest request = new UpdateVariantAttributeRequest("Blue");

        StatusResponse result = controller.update(VARIANT_ID, ATTRIBUTE_ID, request);

        assertEquals("UPDATED", result.status());
        verify(service).updateAttribute(ATTRIBUTE_ID, "Blue");
    }

    @Test
    void update_WhenInvalidValue_ShouldPropagateException() {
        UpdateVariantAttributeRequest request = new UpdateVariantAttributeRequest("Invalid");
        doThrow(new IllegalArgumentException("Invalid attribute value"))
                .when(service).updateAttribute(ATTRIBUTE_ID, "Invalid");

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(VARIANT_ID, ATTRIBUTE_ID, request));
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    void delete_WhenCalled_ShouldReturnDeletedStatus() {
        StatusResponse result = controller.delete(VARIANT_ID, ATTRIBUTE_ID);

        assertEquals("DELETED", result.status());
        verify(service).deleteAttribute(ATTRIBUTE_ID);
    }

    @Test
    void delete_WhenNotFound_ShouldPropagateException() {
        doThrow(new IllegalArgumentException("Variant attribute not found"))
                .when(service).deleteAttribute(ATTRIBUTE_ID);

        assertThrows(IllegalArgumentException.class,
                () -> controller.delete(VARIANT_ID, ATTRIBUTE_ID));
    }
}
