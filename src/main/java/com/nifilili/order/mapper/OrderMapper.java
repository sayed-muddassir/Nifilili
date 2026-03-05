package com.nifilili.order.mapper;

import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.dto.response.OrderItemDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "status", target = "status")
    OrderSummaryResponse toSummary(OrderEntity entity);

    @Mapping(source = "id", target = "orderItemId")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "timeline", ignore = true)
    OrderItemDetailsResponse toItemDetails(OrderItemEntity entity);
}
