package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.AttributeType;
import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.dto.request.VariantAttributeItem;
import com.nifilili.offering.dto.response.VariantAttributeDetailResponse;
import com.nifilili.offering.repository.OfferingAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariantAttributeServiceImplTest {

    private static final long VARIANT_ID = 50L;
    private static final long ATTRIBUTE_ID = 20L;
    private static final long VARIANT_ATTR_ID = 300L;

    @Mock
    private OfferingVariantAttributeRepository variantAttributeRepository;

    @Mock
    private OfferingVariantRepository variantRepository;

    @Mock
    private OfferingAttributeRepository attributeRepository;

    @InjectMocks
    private VariantAttributeServiceImpl variantAttributeService;

    // ── assignAttributes ─────────────────────────────────────────────

    @Test
    void assignAttributes_WhenPredefinedAttribute_ShouldValidateAndSave() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Color");
        predefined.setAttributeType(AttributeType.DROPDOWN);
        predefined.setOptions(List.of("Red", "Blue", "Green"));

        VariantAttributeItem item = new VariantAttributeItem(ATTRIBUTE_ID, "Color", "Red");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(predefined));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.assignAttributes(VARIANT_ID, request);

        ArgumentCaptor<OfferingVariantAttributeEntity> captor =
                ArgumentCaptor.forClass(OfferingVariantAttributeEntity.class);
        verify(variantAttributeRepository).save(captor.capture());

        OfferingVariantAttributeEntity saved = captor.getValue();
        assertEquals(variant, saved.getVariant());
        assertEquals("Color", saved.getAttributeName());
        assertEquals("Red", saved.getAttributeValue());
        assertEquals(predefined, saved.getOfferingAttribute());
    }

    @Test
    void assignAttributes_WhenCustomAttribute_ShouldSaveWithNullOfferingAttribute() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        VariantAttributeItem item = new VariantAttributeItem(null, "CustomField", "CustomValue");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.assignAttributes(VARIANT_ID, request);

        ArgumentCaptor<OfferingVariantAttributeEntity> captor =
                ArgumentCaptor.forClass(OfferingVariantAttributeEntity.class);
        verify(variantAttributeRepository).save(captor.capture());

        OfferingVariantAttributeEntity saved = captor.getValue();
        assertEquals("CustomField", saved.getAttributeName());
        assertEquals("CustomValue", saved.getAttributeValue());
        assertNull(saved.getOfferingAttribute());
    }

    @Test
    void assignAttributes_WhenMultipleAttributes_ShouldSaveAll() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Color");
        predefined.setOptions(List.of("Red", "Blue"));

        VariantAttributeItem item1 = new VariantAttributeItem(ATTRIBUTE_ID, "Color", "Red");
        VariantAttributeItem item2 = new VariantAttributeItem(null, "Weight", "500g");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item1, item2));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(predefined));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.assignAttributes(VARIANT_ID, request);

        verify(variantAttributeRepository, times(2)).save(any(OfferingVariantAttributeEntity.class));
    }

    @Test
    void assignAttributes_WhenVariantNotFound_ShouldThrow() {
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.empty());

        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.assignAttributes(VARIANT_ID, request));
        assertTrue(ex.getMessage().contains("Variant not found"));
        verify(variantAttributeRepository, never()).save(any());
    }

    @Test
    void assignAttributes_WhenPredefinedAttributeNotFound_ShouldThrow() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        VariantAttributeItem item = new VariantAttributeItem(ATTRIBUTE_ID, "Color", "Red");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.assignAttributes(VARIANT_ID, request));
        assertTrue(ex.getMessage().contains("Attribute not found"));
    }

    @Test
    void assignAttributes_WhenAttributeNameMismatch_ShouldThrow() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Color");
        predefined.setOptions(List.of("Red", "Blue"));

        VariantAttributeItem item = new VariantAttributeItem(ATTRIBUTE_ID, "Size", "Red");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(predefined));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.assignAttributes(VARIANT_ID, request));
        assertEquals("Attribute name does not match with the attribute id provided", ex.getMessage());
    }

    @Test
    void assignAttributes_WhenInvalidOptionValue_ShouldThrow() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Color");
        predefined.setOptions(List.of("Red", "Blue", "Green"));

        VariantAttributeItem item = new VariantAttributeItem(ATTRIBUTE_ID, "Color", "Yellow");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(predefined));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.assignAttributes(VARIANT_ID, request));
        assertTrue(ex.getMessage().contains("Invalid attribute value"));
        assertTrue(ex.getMessage().contains("Red, Blue, Green"));
    }

    @Test
    void assignAttributes_WhenPredefinedHasNoOptions_ShouldAllowAnyValue() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Description");
        predefined.setOptions(null); // no options constraint

        VariantAttributeItem item = new VariantAttributeItem(ATTRIBUTE_ID, "Description", "Anything goes");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(predefined));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.assignAttributes(VARIANT_ID, request);

        verify(variantAttributeRepository).save(any(OfferingVariantAttributeEntity.class));
    }

    @Test
    void assignAttributes_WhenPredefinedHasEmptyOptions_ShouldAllowAnyValue() {
        OfferingVariantEntity variant = new OfferingVariantEntity();
        TestEntityIdUtil.withId(variant, VARIANT_ID);

        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Notes");
        predefined.setOptions(List.of()); // empty options list

        VariantAttributeItem item = new VariantAttributeItem(ATTRIBUTE_ID, "Notes", "Free text");
        AssignVariantAttributesRequest request = new AssignVariantAttributesRequest(List.of(item));

        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(predefined));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.assignAttributes(VARIANT_ID, request);

        verify(variantAttributeRepository).save(any(OfferingVariantAttributeEntity.class));
    }

    // ── listAttributes ───────────────────────────────────────────────

    @Test
    void listAttributes_WhenAttributesExist_ShouldReturnMappedResponses() {
        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);

        OfferingVariantAttributeEntity attr1 = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr1, VARIANT_ATTR_ID);
        attr1.setOfferingAttribute(predefined);
        attr1.setAttributeName("Color");
        attr1.setAttributeValue("Red");

        OfferingVariantAttributeEntity attr2 = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr2, 301L);
        attr2.setOfferingAttribute(null); // custom attribute
        attr2.setAttributeName("CustomField");
        attr2.setAttributeValue("CustomValue");

        when(variantAttributeRepository.findByVariantId(VARIANT_ID)).thenReturn(List.of(attr1, attr2));

        List<VariantAttributeDetailResponse> result = variantAttributeService.listAttributes(VARIANT_ID);

        assertEquals(2, result.size());

        // Predefined attribute should include offering attribute id
        VariantAttributeDetailResponse first = result.get(0);
        assertEquals(VARIANT_ATTR_ID, first.id());
        assertEquals(ATTRIBUTE_ID, first.offeringAttributeId());
        assertEquals("Color", first.attributeName());
        assertEquals("Red", first.attributeValue());

        // Custom attribute should have null offering attribute id
        VariantAttributeDetailResponse second = result.get(1);
        assertEquals(301L, second.id());
        assertNull(second.offeringAttributeId());
        assertEquals("CustomField", second.attributeName());
    }

    @Test
    void listAttributes_WhenNoAttributes_ShouldReturnEmptyList() {
        when(variantAttributeRepository.findByVariantId(VARIANT_ID)).thenReturn(List.of());

        List<VariantAttributeDetailResponse> result = variantAttributeService.listAttributes(VARIANT_ID);

        assertTrue(result.isEmpty());
    }

    // ── updateAttribute ──────────────────────────────────────────────

    @Test
    void updateAttribute_WhenCustomAttribute_ShouldUpdateValue() {
        OfferingVariantAttributeEntity attr = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr, VARIANT_ATTR_ID);
        attr.setOfferingAttribute(null); // custom attribute
        attr.setAttributeName("Custom");
        attr.setAttributeValue("OldValue");

        when(variantAttributeRepository.findById(VARIANT_ATTR_ID)).thenReturn(Optional.of(attr));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.updateAttribute(VARIANT_ATTR_ID, "NewValue");

        assertEquals("NewValue", attr.getAttributeValue());
        verify(variantAttributeRepository).save(attr);
    }

    @Test
    void updateAttribute_WhenPredefinedWithValidOption_ShouldUpdateValue() {
        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Color");
        predefined.setOptions(List.of("Red", "Blue", "Green"));

        OfferingVariantAttributeEntity attr = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr, VARIANT_ATTR_ID);
        attr.setOfferingAttribute(predefined);
        attr.setAttributeName("Color");
        attr.setAttributeValue("Red");

        when(variantAttributeRepository.findById(VARIANT_ATTR_ID)).thenReturn(Optional.of(attr));
        when(variantAttributeRepository.save(any(OfferingVariantAttributeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        variantAttributeService.updateAttribute(VARIANT_ATTR_ID, "Blue");

        assertEquals("Blue", attr.getAttributeValue());
        verify(variantAttributeRepository).save(attr);
    }

    @Test
    void updateAttribute_WhenPredefinedWithInvalidOption_ShouldThrow() {
        OfferingAttributeEntity predefined = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(predefined, ATTRIBUTE_ID);
        predefined.setName("Color");
        predefined.setOptions(List.of("Red", "Blue", "Green"));

        OfferingVariantAttributeEntity attr = new OfferingVariantAttributeEntity();
        TestEntityIdUtil.withId(attr, VARIANT_ATTR_ID);
        attr.setOfferingAttribute(predefined);
        attr.setAttributeName("Color");
        attr.setAttributeValue("Red");

        when(variantAttributeRepository.findById(VARIANT_ATTR_ID)).thenReturn(Optional.of(attr));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.updateAttribute(VARIANT_ATTR_ID, "Yellow"));
        assertTrue(ex.getMessage().contains("Invalid attribute value"));
        verify(variantAttributeRepository, never()).save(any());
    }

    @Test
    void updateAttribute_WhenNotFound_ShouldThrow() {
        when(variantAttributeRepository.findById(VARIANT_ATTR_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.updateAttribute(VARIANT_ATTR_ID, "Value"));
        assertEquals("Variant attribute not found", ex.getMessage());
    }

    // ── deleteAttribute ──────────────────────────────────────────────

    @Test
    void deleteAttribute_WhenExists_ShouldDelete() {
        when(variantAttributeRepository.existsById(VARIANT_ATTR_ID)).thenReturn(true);

        variantAttributeService.deleteAttribute(VARIANT_ATTR_ID);

        verify(variantAttributeRepository).deleteById(VARIANT_ATTR_ID);
    }

    @Test
    void deleteAttribute_WhenNotFound_ShouldThrow() {
        when(variantAttributeRepository.existsById(VARIANT_ATTR_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> variantAttributeService.deleteAttribute(VARIANT_ATTR_ID));
        assertEquals("Variant attribute not found", ex.getMessage());
        verify(variantAttributeRepository, never()).deleteById(any());
    }
}
