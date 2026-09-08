package com.nifilili.business.mapper;

import com.nifilili.business.domain.SectionDefinition;
import com.nifilili.business.dto.request.CreateSectionRequest;
import com.nifilili.business.dto.response.SectionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    @Mapping(source = "promptText", target = "prompt")
    SectionDefinition toEntity(CreateSectionRequest request);

    SectionResponse toResponse(SectionDefinition entity);
}
