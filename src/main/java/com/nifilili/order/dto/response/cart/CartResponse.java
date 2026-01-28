package com.nifilili.order.dto.response.cart;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private CartSummaryResponse summary;
}

