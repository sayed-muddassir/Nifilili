package com.nifilili.business.mapper;

import com.nifilili.business.domain.CategoryDefinition;
import com.nifilili.business.dto.request.CreateCategoryRequest;
import com.nifilili.business.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDefinition toEntity(CreateCategoryRequest request);

    CategoryResponse toResponse(CategoryDefinition entity);
}
