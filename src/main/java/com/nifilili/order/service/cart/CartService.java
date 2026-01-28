package com.nifilili.order.service.cart;

import com.nifilili.order.dto.request.cart.AddCartItemRequest;
import com.nifilili.order.dto.request.cart.UpdateCartItemRequest;
import com.nifilili.order.dto.response.cart.CartResponse;
import com.nifilili.order.model.CartSnapshot;

public interface CartService {
    CartResponse getCurrentCart();
    void addItem(AddCartItemRequest request);
    void updateQuantity(Long cartItemId, UpdateCartItemRequest request);
    void removeItem(Long cartItemId);
    void clearCart();
    CartSnapshot getValidatedCartSnapshot();
}

