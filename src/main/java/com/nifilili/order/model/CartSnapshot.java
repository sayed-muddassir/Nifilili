package com.nifilili.order.model;

import com.nifilili.order.domain.CartItemEntity;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CartSnapshot represents an IMMUTABLE, VALIDATED view of the cart
 * at a specific point in time (checkout / order placement).
 *
 * ⚠️ IMPORTANT:
 * - No JPA entities inside
 * - No setters exposed
 * - Safe to use inside transactions
 */
public class CartSnapshot {

    /**
     * All cart items in snapshot form.
     */
    private final List<CartItemSnapshot> items;

    /**
     * Business IDs involved in this cart.
     * Useful for:
     * - business config lookup
     * - coupon validation
     * - delivery/tax calculation
     */
    private final Set<Long> businessIds;

    private CartSnapshot(
            List<CartItemSnapshot> items,
            Set<Long> businessIds
    ) {
        this.items = Collections.unmodifiableList(items);
        this.businessIds = Collections.unmodifiableSet(businessIds);
    }

    // ------------------------------------------------------------
    // Factory method
    // ------------------------------------------------------------

    /**
     * Create a cart snapshot from cart items.
     *
     * This method is the ONLY place where:
     * - cart items are transformed
     * - product/variant data is enriched
     * - availability is validated
     */
    public static CartSnapshot from(List<CartItemEntity> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Cannot create snapshot from empty cart");
        }

        List<CartItemSnapshot> snapshots = new ArrayList<>();
        Set<Long> businessIds = new HashSet<>();

        for (CartItemEntity item : items) {

            /**
             * In real system, THIS is where you would:
             * - call Offering/Product service
             * - validate availability
             * - fetch current price
             * - fetch businessId
             *
             * For now, we mock these values cleanly.
             */

            Long businessId = mockBusinessId(item);
            BigDecimal unitPrice = mockUnitPrice(item);

            CartItemSnapshot snapshot = CartItemSnapshot.builder()
                    .cartItemId(item.getId())
                    .offeringId(item.getOfferingId())
                    .variantId(item.getVariantId())
                    .businessId(businessId)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            snapshots.add(snapshot);
            businessIds.add(businessId);
        }

        return new CartSnapshot(snapshots, businessIds);
    }

    // ------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------

    /**
     * @return immutable list of cart items
     */
    public List<CartItemSnapshot> getItems() {
        return items;
    }

    /**
     * @return unique business IDs involved in this cart
     */
    public Set<Long> getBusinessIds() {
        return businessIds;
    }

    /**
     * Group items by business.
     * Used heavily during pricing & checkout.
     */
    public Map<Long, List<CartItemSnapshot>> itemsByBusiness() {
        return items.stream()
                .collect(Collectors.groupingBy(CartItemSnapshot::getBusinessId));
    }

    /**
     * Calculate subtotal without tax/discount/delivery.
     * Pure utility.
     */
    public BigDecimal calculateSubtotal() {
        return items.stream()
                .map(CartItemSnapshot::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ------------------------------------------------------------
    // Mock helpers (replace with real integrations later)
    // ------------------------------------------------------------

    /**
     * Temporary placeholder.
     * Replace with actual offering → business lookup.
     */
    private static Long mockBusinessId(CartItemEntity item) {
        return 1L; // TODO integrate with Offering service
    }

    /**
     * Temporary placeholder.
     * Replace with actual price lookup.
     */
    private static BigDecimal mockUnitPrice(CartItemEntity item) {
        return BigDecimal.valueOf(500); // TODO integrate with Pricing service
    }
}
