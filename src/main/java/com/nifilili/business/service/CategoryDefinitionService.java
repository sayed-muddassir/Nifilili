package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateCategoryRequest;
import com.nifilili.business.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryDefinitionService {

    CategoryResponse create(CreateCategoryRequest request);
    CategoryResponse update(Long categoryId, CreateCategoryRequest request);

    List<CategoryResponse> getByVertical(Long verticalId);
    List<CategoryResponse> getAll();
}
