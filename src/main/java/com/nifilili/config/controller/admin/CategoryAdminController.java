package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateCategoryRequest;
import com.nifilili.config.dto.response.CategoryResponse;
import com.nifilili.config.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService service;

    @PostMapping
    public CategoryResponse create(@RequestBody CreateCategoryRequest request) {
        return service.create(request);
    }

    @GetMapping("/vertical/{verticalId}")
    public List<CategoryResponse> getByVertical(@PathVariable Long verticalId) {
        return service.getByVertical(verticalId);
    }
}
