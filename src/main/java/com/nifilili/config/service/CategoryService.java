package com.nifilili.config.service;

import com.nifilili.config.dto.request.CreateCategoryRequest;
import com.nifilili.config.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    List<CategoryResponse> getByVertical(Long verticalId);
}
