package com.nifilili.order.dto.request.cart;

import lombok.Data;

@Data
public class AddCartItemRequest {
    private Long offeringId;
    private Long variantId;
    private Integer quantity;
}
