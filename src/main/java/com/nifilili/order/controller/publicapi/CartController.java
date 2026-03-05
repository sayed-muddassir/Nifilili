package com.nifilili.order.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.AddCartItemRequest;
import com.nifilili.order.dto.request.UpdateCartItemRequest;
import com.nifilili.order.dto.response.CartResponse;
import com.nifilili.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_1, description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Returns the authenticated user's cart with enriched item details")
    @ApiResponse(responseCode = "200", description = "Cart returned")
    public ResponseEntity<CartResponse> getCart() {
        log.info("GET /api/v1/cart");
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds an item to the cart or increments quantity if already present")
    @ApiResponse(responseCode = "200", description = "Item added")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        log.info("POST /api/v1/cart/items - offeringId={}", request.getOfferingId());
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific cart item")
    @ApiResponse(responseCode = "200", description = "Quantity updated")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long cartItemId,
                                                        @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT /api/v1/cart/items/{}", cartItemId);
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item", description = "Removes an item from the cart")
    @ApiResponse(responseCode = "204", description = "Item removed")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        log.info("DELETE /api/v1/cart/items/{}", cartItemId);
        cartService.removeItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    @ApiResponse(responseCode = "204", description = "Cart cleared")
    public ResponseEntity<Void> clearCart() {
        log.info("DELETE /api/v1/cart");
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
