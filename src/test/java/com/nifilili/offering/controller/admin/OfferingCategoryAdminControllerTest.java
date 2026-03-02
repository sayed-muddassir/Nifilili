package com.nifilili.offering.controller.admin;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.dto.request.CreateCategoryRequest;
import com.nifilili.offering.dto.request.UpdateCategoryRequest;
import com.nifilili.offering.dto.response.CategoryResponse;
import com.nifilili.offering.dto.response.CategoryTreeResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.OfferingCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferingCategoryAdminControllerTest {

    private static final long CATEGORY_ID = 10L;
    private static final long PARENT_ID = 5L;

    @Mock
    private OfferingCategoryService categoryService;

    @InjectMocks
    private OfferingCategoryAdminController controller;

    // ── create ───────────────────────────────────────────────────────

    @Test
    void create_WhenRootCategory_ShouldReturnResponseWithNullParent() {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", null);
        OfferingCategoryEntity saved = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(saved, CATEGORY_ID);
        saved.setName("Electronics");
        saved.setParentCategory(null);

        when(categoryService.create("Electronics", null)).thenReturn(saved);

        CategoryResponse result = controller.create(request);

        assertEquals(CATEGORY_ID, result.id());
        assertEquals("Electronics", result.name());
        assertNull(result.parentCategoryId());
    }

    @Test
    void create_WhenChildCategory_ShouldReturnResponseWithParentId() {
        CreateCategoryRequest request = new CreateCategoryRequest("Phones", PARENT_ID);

        OfferingCategoryEntity parent = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(parent, PARENT_ID);

        OfferingCategoryEntity saved = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(saved, CATEGORY_ID);
        saved.setName("Phones");
        saved.setParentCategory(parent);

        when(categoryService.create("Phones", PARENT_ID)).thenReturn(saved);

        CategoryResponse result = controller.create(request);

        assertEquals(CATEGORY_ID, result.id());
        assertEquals(PARENT_ID, result.parentCategoryId());
    }

    @Test
    void create_WhenParentNotFound_ShouldPropagateException() {
        CreateCategoryRequest request = new CreateCategoryRequest("Child", 999L);
        when(categoryService.create("Child", 999L))
                .thenThrow(new IllegalArgumentException("Parent category not found"));

        assertThrows(IllegalArgumentException.class, () -> controller.create(request));
    }

    // ── tree ─────────────────────────────────────────────────────────

    @Test
    void tree_WhenCategoriesExist_ShouldReturnMappedTree() {
        OfferingCategoryEntity cat1 = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(cat1, 1L);
        cat1.setName("Electronics");

        OfferingCategoryEntity cat2 = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(cat2, 2L);
        cat2.setName("Clothing");

        when(categoryService.getTree()).thenReturn(List.of(cat1, cat2));

        List<CategoryTreeResponse> result = controller.tree();

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals("Clothing", result.get(1).name());
    }

    @Test
    void tree_WhenEmpty_ShouldReturnEmptyList() {
        when(categoryService.getTree()).thenReturn(List.of());

        List<CategoryTreeResponse> result = controller.tree();

        assertTrue(result.isEmpty());
    }

    // ── update ───────────────────────────────────────────────────────

    @Test
    void update_WhenValid_ShouldReturnUpdatedResponse() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name");
        OfferingCategoryEntity updated = new OfferingCategoryEntity();
        TestEntityIdUtil.withId(updated, CATEGORY_ID);
        updated.setName("Updated Name");
        updated.setParentCategory(null);

        when(categoryService.update(CATEGORY_ID, "Updated Name")).thenReturn(updated);

        CategoryResponse result = controller.update(CATEGORY_ID, request);

        assertEquals(CATEGORY_ID, result.id());
        assertEquals("Updated Name", result.name());
        assertNull(result.parentCategoryId());
    }

    @Test
    void update_WhenNotFound_ShouldPropagateException() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Name");
        when(categoryService.update(CATEGORY_ID, "Name"))
                .thenThrow(new IllegalArgumentException("Category not found"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(CATEGORY_ID, request));
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    void delete_WhenValid_ShouldReturnDeletedStatus() {
        StatusResponse result = controller.delete(CATEGORY_ID);

        assertEquals("DELETED", result.status());
        verify(categoryService).delete(CATEGORY_ID);
    }

    @Test
    void delete_WhenHasChildren_ShouldPropagateException() {
        doThrow(new IllegalStateException("Cannot delete category with child categories"))
                .when(categoryService).delete(CATEGORY_ID);

        assertThrows(IllegalStateException.class,
                () -> controller.delete(CATEGORY_ID));
    }

    @Test
    void delete_WhenHasOfferings_ShouldPropagateException() {
        doThrow(new IllegalStateException("Cannot delete category with existing offerings"))
                .when(categoryService).delete(CATEGORY_ID);

        assertThrows(IllegalStateException.class,
                () -> controller.delete(CATEGORY_ID));
    }
}
