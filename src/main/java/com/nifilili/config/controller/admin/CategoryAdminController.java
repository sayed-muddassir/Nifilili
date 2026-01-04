package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateCategoryRequest;
import com.nifilili.config.dto.response.CategoryResponse;
import com.nifilili.config.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/categories")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryService service;

    @Operation(summary = "Create Category",
            description = "Creates a new category for a specific vertical.",
            tags = {"Category Management [Admin]"}
    )
    @PostMapping
    public CategoryResponse create(@RequestBody CreateCategoryRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Get Categories by Vertical",
            description = "Retrieves all categories associated with a specific vertical.",
            tags = {"Category Management [Admin]"}
    )
    @GetMapping("/vertical/{verticalId}")
    public List<CategoryResponse> getByVertical(@PathVariable Long verticalId) {
        return service.getByVertical(verticalId);
    }
}
