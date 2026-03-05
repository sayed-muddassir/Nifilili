package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CheckoutBusinessGroupResponse {
    private Long businessId;
    private String businessName;
    private List<CheckoutItemResponse> items;
    private BigDecimal deliveryCharge;
    private BigDecimal tax;
    private BigDecimal discount;
}
