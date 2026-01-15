package com.nifilili.offering.controller.admin;

import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.dto.request.CreateCategoryRequest;
import com.nifilili.offering.dto.response.CategoryResponse;
import com.nifilili.offering.dto.response.CategoryTreeResponse;
import com.nifilili.offering.service.admin.OfferingCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/offering-categories")
@RequiredArgsConstructor
public class OfferingCategoryAdminController {

    private final OfferingCategoryService categoryService;

    @PostMapping
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
    public List<CategoryTreeResponse> tree() {
        return categoryService.getTree()
                .stream()
                .map(this::toTree)
                .toList();
    }

    private CategoryTreeResponse toTree(OfferingCategoryEntity entity) {
        return new CategoryTreeResponse(
                entity.getId(),
                entity.getName(),
                List.of() // children resolved recursively in UI / service if needed
        );
    }
}


