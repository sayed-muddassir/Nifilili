package com.nifilili.order.service;

import com.nifilili.order.dto.request.CheckoutPreviewRequest;
import com.nifilili.order.dto.response.CheckoutPreviewResponse;

public interface CheckoutService {

    /**
     * Generates a checkout preview by enriching the user's cart, grouping items by business,
     * applying coupons, and calculating taxes, delivery charges, and discounts.
     *
     * @param request the checkout preview request with address and optional coupons
     * @return the checkout preview with business groups and pricing summary
     * @throws com.nifilili.core.exception.InvalidOrderStateException if cart is empty
     */
    CheckoutPreviewResponse preview(CheckoutPreviewRequest request);
}
