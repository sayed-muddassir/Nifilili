package com.nifilili.order.controller.cart;

import com.nifilili.order.dto.request.cart.AddCartItemRequest;
import com.nifilili.order.dto.request.cart.UpdateCartItemRequest;
import com.nifilili.order.dto.response.cart.CartResponse;
import com.nifilili.order.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCurrentCart();
    }

    @PostMapping("/items")
    public void addItem(@RequestBody AddCartItemRequest request) {
        cartService.addItem(request);
    }

    @PutMapping("/items/{cartItemId}")
    public void updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        cartService.updateQuantity(cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    public void removeItem(@PathVariable Long cartItemId) {
        cartService.removeItem(cartItemId);
    }

    @DeleteMapping
    public void clearCart() {
        cartService.clearCart();
    }
}
