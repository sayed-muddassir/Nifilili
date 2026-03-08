package com.nifilili.quote.mapper;

import com.nifilili.quote.domain.QuoteRequestEntity;
import com.nifilili.quote.dto.response.QuoteRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuoteRequestMapper {

    @Mapping(source = "id", target = "requestId")
    @Mapping(source = "status", target = "status")
    QuoteRequestResponse toResponse(QuoteRequestEntity entity);
}
