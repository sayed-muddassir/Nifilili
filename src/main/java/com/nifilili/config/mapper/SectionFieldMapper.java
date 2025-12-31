package com.nifilili.config.mapper;

import com.nifilili.config.domain.SectionField;
import com.nifilili.config.dto.request.CreateSectionFieldRequest;
import com.nifilili.config.dto.response.SectionFieldResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionFieldMapper {

    @Mapping(target = "sectionId", ignore = true)// populated via join later
    SectionField toEntity(CreateSectionFieldRequest request);

    SectionFieldResponse toResponse(SectionField entity);
}
