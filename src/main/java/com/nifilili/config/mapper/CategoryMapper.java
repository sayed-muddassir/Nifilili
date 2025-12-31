package com.nifilili.config.mapper;

import com.nifilili.config.domain.Category;
import com.nifilili.config.dto.request.CreateCategoryRequest;
import com.nifilili.config.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequest request);

    CategoryResponse toResponse(Category entity);
}
