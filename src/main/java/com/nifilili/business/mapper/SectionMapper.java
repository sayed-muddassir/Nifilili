package com.nifilili.business.mapper;

import com.nifilili.business.domain.SectionDefinition;
import com.nifilili.business.dto.request.CreateSectionRequest;
import com.nifilili.business.dto.response.SectionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    SectionDefinition toEntity(CreateSectionRequest request);

    SectionResponse toResponse(SectionDefinition entity);
}
