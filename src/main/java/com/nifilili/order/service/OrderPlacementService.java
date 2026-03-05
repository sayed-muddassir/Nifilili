package com.nifilili.order.service;

import com.nifilili.order.dto.request.PlaceOrderRequest;
import com.nifilili.order.dto.response.PlaceOrderResponse;

public interface OrderPlacementService {

    /**
     * Places an order atomically: snapshots cart items with current prices, applies coupons,
     * calculates taxes and delivery, creates the order with items and payment, records coupon
     * usage, clears the cart, and publishes an OrderPlacedEvent.
     *
     * @param request the order placement request with delivery address, payment type, and optional coupons
     * @return the placed order summary with order number and payment status
     * @throws com.nifilili.core.exception.InvalidOrderStateException if cart is empty
     * @throws com.nifilili.core.exception.ResourceNotFoundException  if payment type not found
     */
    PlaceOrderResponse placeOrder(PlaceOrderRequest request);
}
