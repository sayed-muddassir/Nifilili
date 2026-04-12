package com.nifilili.business.service.impl;

import com.nifilili.business.domain.CategoryDefinition;
import com.nifilili.business.dto.request.CreateCategoryRequest;
import com.nifilili.business.dto.response.CategoryResponse;
import com.nifilili.business.mapper.CategoryMapper;
import com.nifilili.business.repository.CategoryRepository;
import com.nifilili.business.service.CategoryDefinitionService;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryDefinitionServiceImpl implements CategoryDefinitionService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        CategoryDefinition saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public CategoryResponse update(Long categoryId, CreateCategoryRequest request) {
        CategoryDefinition existing = repository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        existing.setBusinessVerticalId(request.getBusinessVerticalId());
        existing.setParentCategoryId(request.getParentCategoryId());
        existing.setName(request.getName());
        existing.setSlug(request.getSlug());
        existing.setDescription(request.getDescription());
        existing.setIconUrl(request.getIconUrl());
        existing.setActiveStatus(request.isActiveStatus());

        return mapper.toResponse(repository.save(existing));
    }

    @Override
    public List<CategoryResponse> getByVertical(Long verticalId) {
        return repository.findByBusinessVerticalId(verticalId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
