package com.nifilili.offering.controller.admin;

import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.dto.request.CreateCategoryRequest;
import com.nifilili.offering.dto.request.UpdateCategoryRequest;
import com.nifilili.offering.dto.response.CategoryResponse;
import com.nifilili.offering.dto.response.CategoryTreeResponse;
import com.nifilili.offering.dto.response.StatusResponse;
import com.nifilili.offering.service.OfferingCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/offering-categories")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = "Offering Category Admin", description = "Admin management of product and service categories")
public class OfferingCategoryAdminController {

    private final OfferingCategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category", description = "Creates a new offering category, optionally as a child of an existing category")
    @ApiResponse(responseCode = "200", description = "Category created")
    @ApiResponse(responseCode = "400", description = "Parent category not found")
    public CategoryResponse create(@RequestBody CreateCategoryRequest request) {
        OfferingCategoryEntity saved =
                categoryService.create(request.name(), request.parentCategoryId());

        return new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getParentCategory() != null ? saved.getParentCategory().getId() : null
        );
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Returns the hierarchical tree of all offering categories")
    @ApiResponse(responseCode = "200", description = "Category tree returned")
    public List<CategoryTreeResponse> tree() {
        return categoryService.getTree()
                .stream()
                .map(this::toTree)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates the name of an existing offering category")
    @ApiResponse(responseCode = "200", description = "Category updated")
    @ApiResponse(responseCode = "400", description = "Category not found")
    public CategoryResponse update(@PathVariable Long id, @RequestBody UpdateCategoryRequest request) {
        log.info("PUT /api/v1/admin/offering-categories/{}", id);
        OfferingCategoryEntity updated = categoryService.update(id, request.name());
        return new CategoryResponse(
                updated.getId(),
                updated.getName(),
                updated.getParentCategory() != null ? updated.getParentCategory().getId() : null
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category",
            description = "Deletes a category if it has no child categories and no offerings assigned to it")
    @ApiResponse(responseCode = "200", description = "Category deleted")
    @ApiResponse(responseCode = "400", description = "Category not found or has children/offerings")
    public StatusResponse delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/admin/offering-categories/{}", id);
        categoryService.delete(id);
        return new StatusResponse("DELETED");
    }

    private CategoryTreeResponse toTree(OfferingCategoryEntity entity) {
        return new CategoryTreeResponse(
                entity.getId(),
                entity.getName(),
                List.of() // children resolved recursively in UI / service if needed
        );
    }
}


