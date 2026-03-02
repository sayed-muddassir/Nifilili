package com.nifilili.offering.controller.admin;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.AttributeType;
import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.dto.request.CreateAttributeRequest;
import com.nifilili.offering.dto.request.UpdateAttributeRequest;
import com.nifilili.offering.dto.response.AttributeResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.OfferingAttributeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingAttributeAdminControllerTest {

    private static final long ATTRIBUTE_ID = 20L;
    private static final long CATEGORY_ID = 10L;

    @Mock
    private OfferingAttributeService attributeService;

    @InjectMocks
    private OfferingAttributeAdminController controller;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenValid_ShouldReturnAttributeResponse() {
        List<String> options = List.of("Red", "Blue", "Green");
        CreateAttributeRequest request = new CreateAttributeRequest(
                CATEGORY_ID, "Color", AttributeType.DROPDOWN, options
        );

        OfferingAttributeEntity saved = buildAttribute("Color", AttributeType.DROPDOWN, options);
        when(attributeService.create(CATEGORY_ID, "Color", AttributeType.DROPDOWN, options))
                .thenReturn(saved);

        AttributeResponse result = controller.create(request);

        assertEquals(ATTRIBUTE_ID, result.id());
        assertEquals(CATEGORY_ID, result.offeringCategoryId());
        assertEquals("Color", result.name());
        assertEquals(AttributeType.DROPDOWN, result.attributeType());
        assertEquals(options, result.options());
    }

    @Test
    void create_WhenCategoryNotFound_ShouldPropagateException() {
        CreateAttributeRequest request = new CreateAttributeRequest(
                999L, "Color", AttributeType.DROPDOWN, List.of()
        );
        when(attributeService.create(999L, "Color", AttributeType.DROPDOWN, List.of()))
                .thenThrow(new IllegalArgumentException("Category not found"));

        assertThrows(IllegalArgumentException.class, () -> controller.create(request));
    }

    // ── byCategory ───────────────────────────────────────────────────

    @Test
    void byCategory_WhenAttributesExist_ShouldReturnMappedList() {
        OfferingAttributeEntity attr1 = buildAttribute("Color", AttributeType.DROPDOWN, List.of("Red", "Blue"));
        OfferingAttributeEntity attr2 = buildAttribute("Size", AttributeType.TEXT, null);
        TestEntityIdUtil.withId(attr2, 21L);

        when(attributeService.getByCategory(CATEGORY_ID)).thenReturn(List.of(attr1, attr2));

        List<AttributeResponse> result = controller.byCategory(CATEGORY_ID);

        assertEquals(2, result.size());
        assertEquals("Color", result.get(0).name());
        assertEquals("Size", result.get(1).name());
    }

    @Test
    void byCategory_WhenEmpty_ShouldReturnEmptyList() {
        when(attributeService.getByCategory(CATEGORY_ID)).thenReturn(List.of());

        List<AttributeResponse> result = controller.byCategory(CATEGORY_ID);

        assertTrue(result.isEmpty());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenValid_ShouldReturnUpdatedResponse() {
        List<String> newOptions = List.of("S", "M", "L");
        UpdateAttributeRequest request = new UpdateAttributeRequest("Size", AttributeType.TEXT, newOptions);

        OfferingAttributeEntity updated = buildAttribute("Size", AttributeType.TEXT, newOptions);
        when(attributeService.update(ATTRIBUTE_ID, "Size", AttributeType.TEXT, newOptions))
                .thenReturn(updated);

        AttributeResponse result = controller.update(ATTRIBUTE_ID, request);

        assertEquals("Size", result.name());
        assertEquals(AttributeType.TEXT, result.attributeType());
        assertEquals(newOptions, result.options());
    }

    @Test
    void update_WhenNotFound_ShouldPropagateException() {
        UpdateAttributeRequest request = new UpdateAttributeRequest("Name", AttributeType.TEXT, List.of());
        when(attributeService.update(ATTRIBUTE_ID, "Name", AttributeType.TEXT, List.of()))
                .thenThrow(new IllegalArgumentException("Attribute not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(ATTRIBUTE_ID, request));
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    void delete_WhenValid_ShouldReturnDeletedStatus() {
        StatusResponse result = controller.delete(ATTRIBUTE_ID);

        assertEquals("DELETED", result.status());
        verify(attributeService).delete(ATTRIBUTE_ID);
    }

    @Test
    void delete_WhenInUse_ShouldPropagateException() {
        doThrow(new IllegalStateException("Cannot delete attribute in use by variant attributes"))
                .when(attributeService).delete(ATTRIBUTE_ID);

        assertThrows(IllegalStateException.class,
                () -> controller.delete(ATTRIBUTE_ID));
    }

    @Test
    void delete_WhenNotFound_ShouldPropagateException() {
        doThrow(new IllegalArgumentException("Attribute not found"))
                .when(attributeService).delete(ATTRIBUTE_ID);

        assertThrows(IllegalArgumentException.class,
                () -> controller.delete(ATTRIBUTE_ID));
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingAttributeEntity buildAttribute(String name, AttributeType type, List<String> options) {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);

        OfferingAttributeEntity entity = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(entity, ATTRIBUTE_ID);
        entity.setCategory(category);
        entity.setName(name);
        entity.setAttributeType(type);
        entity.setOptions(options);
        return entity;
    }
}
