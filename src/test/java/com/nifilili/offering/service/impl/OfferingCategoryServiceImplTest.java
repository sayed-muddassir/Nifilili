package com.nifilili.offering.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.repository.OfferingCategoryRepository;
import com.nifilili.offering.repository.OfferingRepository;
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
class OfferingCategoryServiceImplTest {

    private static final long CATEGORY_ID = 10L;
    private static final long PARENT_CATEGORY_ID = 5L;

    @Mock
    private OfferingCategoryRepository categoryRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @InjectMocks
    private OfferingCategoryServiceImpl categoryService;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenRootCategory_ShouldSaveWithoutParent() {
        when(categoryRepository.save(any(OfferingCategoryEntity.class))).thenAnswer(inv -> {
            OfferingCategoryEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, CATEGORY_ID);
            return saved;
        });

        OfferingCategoryEntity result = categoryService.create("Electronics", null);

        assertEquals("Electronics", result.getName());
        assertNull(result.getParentCategory());

        ArgumentCaptor<OfferingCategoryEntity> captor = ArgumentCaptor.forClass(OfferingCategoryEntity.class);
        verify(categoryRepository).save(captor.capture());
        assertNull(captor.getValue().getParentCategory());
    }

    @Test
    void create_WhenWithParent_ShouldSetParentCategory() {
        OfferingCategoryEntity parent = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(parent, PARENT_CATEGORY_ID);
        parent.setName("Electronics");

        when(categoryRepository.findById(PARENT_CATEGORY_ID)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(OfferingCategoryEntity.class))).thenAnswer(inv -> {
            OfferingCategoryEntity saved = inv.getArgument(0);
            TestEntityIdUtil.withId(saved, CATEGORY_ID);
            return saved;
        });

        OfferingCategoryEntity result = categoryService.create("Phones", PARENT_CATEGORY_ID);

        assertEquals("Phones", result.getName());
        assertEquals(parent, result.getParentCategory());
    }

    @Test
    void create_WhenParentNotFound_ShouldThrow() {
        when(categoryRepository.findById(PARENT_CATEGORY_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.create("Phones", PARENT_CATEGORY_ID));
        assertEquals("Parent category not found", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    // ── getTree ──────────────────────────────────────────────────────

    @Test
    void getTree_WhenRootCategoriesExist_ShouldReturnAll() {
        OfferingCategoryEntity cat1 = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(cat1, 1L);
        cat1.setName("Electronics");

        OfferingCategoryEntity cat2 = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(cat2, 2L);
        cat2.setName("Clothing");

        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(List.of(cat1, cat2));

        List<OfferingCategoryEntity> result = categoryService.getTree();

        assertEquals(2, result.size());
        verify(categoryRepository).findByParentCategoryIsNull();
    }

    @Test
    void getTree_WhenNoCategories_ShouldReturnEmptyList() {
        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(List.of());

        List<OfferingCategoryEntity> result = categoryService.getTree();

        assertTrue(result.isEmpty());
    }

    // ── validateLeafCategory ─────────────────────────────────────────

    @Test
    void validateLeafCategory_WhenLeafCategory_ShouldReturnEntity() {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);
        category.setName("Smartphones");

        when(categoryRepository.existsByParentCategoryId(CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

        OfferingCategoryEntity result = categoryService.validateLeafCategory(CATEGORY_ID);

        assertEquals(CATEGORY_ID, result.getId());
        assertEquals("Smartphones", result.getName());
    }

    @Test
    void validateLeafCategory_WhenHasChildren_ShouldThrow() {
        when(categoryRepository.existsByParentCategoryId(CATEGORY_ID)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoryService.validateLeafCategory(CATEGORY_ID));
        assertEquals("Offering must be assigned to a leaf category", ex.getMessage());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void validateLeafCategory_WhenCategoryNotFound_ShouldThrow() {
        when(categoryRepository.existsByParentCategoryId(CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.validateLeafCategory(CATEGORY_ID));
        assertEquals("Category not found", ex.getMessage());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenCategoryExists_ShouldUpdateName() {
        OfferingCategoryEntity category = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(category, CATEGORY_ID);
        category.setName("Old Name");

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(OfferingCategoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferingCategoryEntity result = categoryService.update(CATEGORY_ID, "New Name");

        assertEquals("New Name", result.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void update_WhenCategoryNotFound_ShouldThrow() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.update(CATEGORY_ID, "New Name"));
        assertEquals("Category not found", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    void delete_WhenSafeToDelete_ShouldDeleteCategory() {
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(categoryRepository.existsByParentCategoryId(CATEGORY_ID)).thenReturn(false);
        when(offeringRepository.existsByCategoryId(CATEGORY_ID)).thenReturn(false);

        categoryService.delete(CATEGORY_ID);

        verify(categoryRepository).deleteById(CATEGORY_ID);
    }

    @Test
    void delete_WhenCategoryNotFound_ShouldThrow() {
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.delete(CATEGORY_ID));
        assertEquals("Category not found", ex.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_WhenHasChildCategories_ShouldThrow() {
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(categoryRepository.existsByParentCategoryId(CATEGORY_ID)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoryService.delete(CATEGORY_ID));
        assertEquals("Cannot delete category with child categories", ex.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_WhenHasOfferings_ShouldThrow() {
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(categoryRepository.existsByParentCategoryId(CATEGORY_ID)).thenReturn(false);
        when(offeringRepository.existsByCategoryId(CATEGORY_ID)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoryService.delete(CATEGORY_ID));
        assertEquals("Cannot delete category with existing offerings", ex.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }
}
