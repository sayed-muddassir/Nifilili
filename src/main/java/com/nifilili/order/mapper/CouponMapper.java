package com.nifilili.order.mapper;

import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {

//    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CouponEntity toEntity(CreateCouponRequest request);

    CouponResponse toResponse(CouponEntity entity);
}
