package com.nifilili.config.mapper;

import com.nifilili.config.domain.Section;
import com.nifilili.config.dto.request.CreateSectionRequest;
import com.nifilili.config.dto.response.SectionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    Section toEntity(CreateSectionRequest request);

    SectionResponse toResponse(Section entity);
}
