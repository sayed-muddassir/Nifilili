package com.nifilili.config.service.impl;

import com.nifilili.config.domain.Category;
import com.nifilili.config.dto.request.CreateCategoryRequest;
import com.nifilili.config.dto.response.CategoryResponse;
import com.nifilili.config.mapper.CategoryMapper;
import com.nifilili.config.repository.CategoryRepository;
import com.nifilili.config.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        Category saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> getByVertical(Long verticalId) {
        return repository.findByBusinessVerticalId(verticalId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
