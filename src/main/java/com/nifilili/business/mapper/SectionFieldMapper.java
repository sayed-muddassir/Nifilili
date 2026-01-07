package com.nifilili.business.mapper;

import com.nifilili.business.domain.SectionField;
import com.nifilili.business.dto.request.CreateSectionFieldRequest;
import com.nifilili.business.dto.response.SectionFieldResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionFieldMapper {

    @Mapping(target = "sectionId", ignore = true)// populated via join later
    SectionField toEntity(CreateSectionFieldRequest request);

    SectionFieldResponse toResponse(SectionField entity);
}
