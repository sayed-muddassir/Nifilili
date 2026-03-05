package com.nifilili.order.mapper;

import com.nifilili.order.domain.OrderRefundEntity;
import com.nifilili.order.dto.response.RefundResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(source = "id", target = "refundId")
    @Mapping(source = "status", target = "status")
    RefundResponse toResponse(OrderRefundEntity entity);
}
