package com.nifilili.order.service;

import com.nifilili.order.dto.request.AddCartItemRequest;
import com.nifilili.order.dto.request.UpdateCartItemRequest;
import com.nifilili.order.dto.response.CartResponse;

public interface CartService {

    /**
     * Retrieves the authenticated user's cart with enriched item details.
     *
     * @return the cart with items and summary
     */
    CartResponse getCart();

    /**
     * Adds an item to the authenticated user's cart. If the same offering+variant already exists,
     * increments the quantity instead of creating a duplicate.
     *
     * @param request the item to add (offering ID, optional variant ID, quantity)
     * @return the updated cart
     */
    CartResponse addItem(AddCartItemRequest request);

    /**
     * Updates the quantity of an existing cart item.
     *
     * @param cartItemId the cart item ID to update
     * @param request    the new quantity
     * @return the updated cart
     * @throws com.nifilili.core.exception.ResourceNotFoundException if cart item not found
     */
    CartResponse updateQuantity(Long cartItemId, UpdateCartItemRequest request);

    /**
     * Removes an item from the authenticated user's cart.
     *
     * @param cartItemId the cart item ID to remove
     * @throws com.nifilili.core.exception.ResourceNotFoundException if cart item not found
     */
    void removeItem(Long cartItemId);

    /**
     * Removes all items from the authenticated user's cart.
     */
    void clearCart();
}
