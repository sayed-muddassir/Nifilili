package com.nifilili.business.mapper;

import com.nifilili.business.domain.BusinessAttribute;
import com.nifilili.business.dto.response.BusinessAttributeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusinessAttributeMapper {

    @Mapping(target = "name", ignore = true)
        // populated via join later
    BusinessAttributeResponse toResponse(BusinessAttribute entity);
}
