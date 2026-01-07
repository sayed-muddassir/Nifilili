package com.nifilili.business.mapper;

import com.nifilili.business.domain.VerticalDefinition;
import com.nifilili.business.dto.request.CreateVerticalRequest;
import com.nifilili.business.dto.response.VerticalResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VerticalMapper {

    VerticalDefinition toEntity(CreateVerticalRequest request);

    VerticalResponse toResponse(VerticalDefinition entity);
}
