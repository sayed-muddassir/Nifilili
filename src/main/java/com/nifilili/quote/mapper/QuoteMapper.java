package com.nifilili.quote.mapper;

import com.nifilili.quote.domain.QuoteEntity;
import com.nifilili.quote.domain.QuoteLineItemEntity;
import com.nifilili.quote.dto.response.QuoteLineItemResponse;
import com.nifilili.quote.dto.response.QuoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuoteMapper {

    @Mapping(source = "id", target = "quoteId")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "lineItems", ignore = true)
    QuoteResponse toResponse(QuoteEntity entity);

    @Mapping(source = "id", target = "lineItemId")
    QuoteLineItemResponse toLineItemResponse(QuoteLineItemEntity entity);
}
