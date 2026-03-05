package com.nifilili.order.service.impl;

import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.service.CartEnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of CartEnrichmentService.
 * TODO: Replace with real offering module integration via events when available.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartEnrichmentServiceImpl implements CartEnrichmentService {

    @Override
    public List<CartItemResponse> enrich(List<CartItemEntity> cartItems) {
        log.debug("Enriching {} cart items with offering data (mock)", cartItems.size());

        return cartItems.stream()
                .map(this::enrichItem)
                .toList();
    }

    private CartItemResponse enrichItem(CartItemEntity item) {
        // TODO: Fetch real offering data via event-driven integration
        BigDecimal unitPrice = new BigDecimal("100.00");
        String title = "Offering #" + item.getOfferingId();
        Long businessId = 1L;
        Map<String, Object> variantAttributes = item.getVariantId() != null
                ? Map.of("variantId", item.getVariantId())
                : Map.of();

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .offeringId(item.getOfferingId())
                .variantId(item.getVariantId())
                .businessId(businessId)
                .title(title)
                .variantAttributes(variantAttributes)
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .available(true)
                .subtotal(subtotal)
                .build();
    }
}
