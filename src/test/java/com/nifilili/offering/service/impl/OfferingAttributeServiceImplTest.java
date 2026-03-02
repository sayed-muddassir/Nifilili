package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.AttributeType;
import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.repository.OfferingAttributeRepository;
import com.nifilili.offering.repository.OfferingCategoryRepository;
import com.nifilili.offering.repository.OfferingVariantAttributeRepository;
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
class OfferingAttributeServiceImplTest {

    private static final long ATTRIBUTE_ID = 20L;
    private static final long CATEGORY_ID = 10L;

    @Mock
    private OfferingAttributeRepository attributeRepository;

    @Mock
    private OfferingCategoryRepository categoryRepository;

    @Mock
    private OfferingVariantAttributeRepository variantAttributeRepository;

    @InjectMocks
    private OfferingAttributeServiceImpl attributeService;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenCategoryExists_ShouldSaveAttribute() {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);
        category.setName("Electronics");

        List<String> options = List.of("Red", "Blue", "Green");

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(attributeRepository.save(any(OfferingAttributeEntity.class))).thenAnswer(inv -> {
            OfferingAttributeEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, ATTRIBUTE_ID);
            return saved;
        });

        OfferingAttributeEntity result = attributeService.create(
                CATEGORY_ID, "Color", AttributeType.DROPDOWN, options
        );

        assertEquals("Color", result.getName());
        assertEquals(AttributeType.DROPDOWN, result.getAttributeType());
        assertEquals(options, result.getOptions());
        assertEquals(category, result.getCategory());

        ArgumentCaptor<OfferingAttributeEntity> captor = ArgumentCaptor.forClass(OfferingAttributeEntity.class);
        verify(attributeRepository).save(captor.capture());
        assertEquals("Color", captor.getValue().getName());
    }

    @Test
    void create_WhenCategoryNotFound_ShouldThrow() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attributeService.create(CATEGORY_ID, "Color", AttributeType.DROPDOWN, List.of()));
        assertEquals("Category not found", ex.getMessage());
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void create_WhenTextTypeWithNoOptions_ShouldSaveSuccessfully() {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(attributeRepository.save(any(OfferingAttributeEntity.class))).thenAnswer(inv -> {
            OfferingAttributeEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, ATTRIBUTE_ID);
            return saved;
        });

        OfferingAttributeEntity result = attributeService.create(
                CATEGORY_ID, "Description", AttributeType.TEXT, null
        );

        assertEquals("Description", result.getName());
        assertEquals(AttributeType.TEXT, result.getAttributeType());
        assertNull(result.getOptions());
    }

    // ── getByCategory ────────────────────────────────────────────────

    @Test
    void getByCategory_WhenAttributesExist_ShouldReturnAll() {
        OfferingAttributeEntity attr1 = buildAttributeEntity("Color", AttributeType.DROPDOWN);
        OfferingAttributeEntity attr2 = buildAttributeEntity("Size", AttributeType.TEXT);
        TestEntityIdUtil.withId(attr2, 21L);

        when(attributeRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(attr1, attr2));

        List<OfferingAttributeEntity> result = attributeService.getByCategory(CATEGORY_ID);

        assertEquals(2, result.size());
        verify(attributeRepository).findByCategoryId(CATEGORY_ID);
    }

    @Test
    void getByCategory_WhenNoAttributes_ShouldReturnEmptyList() {
        when(attributeRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of());

        List<OfferingAttributeEntity> result = attributeService.getByCategory(CATEGORY_ID);

        assertTrue(result.isEmpty());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenAttributeExists_ShouldUpdateAllFields() {
        OfferingAttributeEntity existing = buildAttributeEntity("Color", AttributeType.DROPDOWN);
        existing.setOptions(List.of("Red", "Blue"));

        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(existing));
        when(attributeRepository.save(any(OfferingAttributeEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> newOptions = List.of("S", "M", "L", "XL");
        OfferingAttributeEntity result = attributeService.update(
                ATTRIBUTE_ID, "Size", AttributeType.TEXT, newOptions
        );

        assertEquals("Size", result.getName());
        assertEquals(AttributeType.TEXT, result.getAttributeType());
        assertEquals(newOptions, result.getOptions());
        verify(attributeRepository).save(existing);
    }

    @Test
    void update_WhenAttributeNotFound_ShouldThrow() {
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attributeService.update(ATTRIBUTE_ID, "Size", AttributeType.TEXT, List.of()));
        assertEquals("Attribute not found", ex.getMessage());
        verify(attributeRepository, never()).save(any());
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    void delete_WhenNotInUse_ShouldDeleteAttribute() {
        when(attributeRepository.existsById(ATTRIBUTE_ID)).thenReturn(true);
        when(variantAttributeRepository.existsByOfferingAttributeId(ATTRIBUTE_ID)).thenReturn(false);

        attributeService.delete(ATTRIBUTE_ID);

        verify(attributeRepository).deleteById(ATTRIBUTE_ID);
    }

    @Test
    void delete_WhenAttributeNotFound_ShouldThrow() {
        when(attributeRepository.existsById(ATTRIBUTE_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attributeService.delete(ATTRIBUTE_ID));
        assertEquals("Attribute not found", ex.getMessage());
        verify(attributeRepository, never()).deleteById(any());
    }

    @Test
    void delete_WhenInUseByVariants_ShouldThrow() {
        when(attributeRepository.existsById(ATTRIBUTE_ID)).thenReturn(true);
        when(variantAttributeRepository.existsByOfferingAttributeId(ATTRIBUTE_ID)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> attributeService.delete(ATTRIBUTE_ID));
        assertEquals("Cannot delete attribute in use by variant attributes", ex.getMessage());
        verify(attributeRepository, never()).deleteById(any());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private OfferingAttributeEntity buildAttributeEntity(String name, AttributeType type) {
        OfferingAttributeEntity entity = new OfferingAttributeEntity();
        TestEntityIdUtil.withId(entity, ATTRIBUTE_ID);
        entity.setName(name);
        entity.setAttributeType(type);
        entity.setOptions(List.of("Red", "Blue", "Green"));

        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);
        entity.setCategory(category);

        return entity;
    }
}
