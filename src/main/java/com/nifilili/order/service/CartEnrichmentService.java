package com.nifilili.order.service;

import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.dto.response.CartItemResponse;

import java.util.List;

public interface CartEnrichmentService {

    /**
     * Enriches raw cart items with offering data (title, price, availability, variant attributes).
     * This is a cross-module boundary — the implementation provides mock data until
     * real offering module integration is established via events.
     *
     * @param cartItems the raw cart items from the database
     * @return enriched cart item responses with offering details
     */
    List<CartItemResponse> enrich(List<CartItemEntity> cartItems);
}
