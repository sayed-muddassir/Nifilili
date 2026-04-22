package com.nifilili.business.mapper;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.response.UserCreatedBusinessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusinessMapper {

    @Mapping(target = "businessId", expression = "java(businessEntity.getId())")
    UserCreatedBusinessResponse toDtoResponse(Business businessEntity);
}
