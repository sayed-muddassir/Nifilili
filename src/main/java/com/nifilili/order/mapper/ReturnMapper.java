package com.nifilili.order.mapper;

import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.response.ReturnResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReturnMapper {

    @Mapping(source = "id", target = "returnRequestId")
    @Mapping(source = "status", target = "status")
    ReturnResponse toResponse(ReturnRequestEntity entity);
}
