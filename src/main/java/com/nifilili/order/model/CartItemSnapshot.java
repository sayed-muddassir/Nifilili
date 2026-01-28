package com.nifilili.order.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Immutable snapshot of a single cart item.
 */
@Builder
public class CartItemSnapshot {

    private final Long cartItemId;
    private final Long offeringId;
    private final Long variantId;
    private final Long businessId;
    private final Integer quantity;
    private final BigDecimal unitPrice;

    // ------------------------------------------------------------
    // Getters only (immutable)
    // ------------------------------------------------------------

    public Long getCartItemId() {
        return cartItemId;
    }

    public Long getOfferingId() {
        return offeringId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    // ------------------------------------------------------------
    // Domain logic
    // ------------------------------------------------------------

    /**
     * Subtotal for this item (price × quantity).
     */
    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
