package com.nifilili.config.mapper;

import com.nifilili.config.domain.Vertical;
import com.nifilili.config.dto.request.CreateVerticalRequest;
import com.nifilili.config.dto.response.VerticalResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VerticalMapper {

    Vertical toEntity(CreateVerticalRequest request);

    VerticalResponse toResponse(Vertical entity);
}
