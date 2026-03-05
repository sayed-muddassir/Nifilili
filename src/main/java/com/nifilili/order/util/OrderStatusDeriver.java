package com.nifilili.order.util;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.order.domain.OrderItemEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Derives the overall order status from the statuses of all its items.
 * Business rules:
 * - All DELIVERED → DELIVERED
 * - All CANCELLED → CANCELLED
 * - All REJECTED → REJECTED
 * - Mix of DELIVERED + others → PARTIALLY_DELIVERED
 * - Any SHIPPED (none delivered yet) → SHIPPED
 * - Any PREPARING/RECEIVED → PROCESSING
 * - Mix of terminal + active → PARTIALLY_PROCESSED
 * - Default fallback → PLACED
 */
public final class OrderStatusDeriver {

    private static final Set<OrderItemStatus> TERMINAL_STATUSES = Set.of(
            OrderItemStatus.DELIVERED,
            OrderItemStatus.CANCELLED,
            OrderItemStatus.REJECTED,
            OrderItemStatus.RETURNED
    );

    private OrderStatusDeriver() {
        // utility class
    }

    /**
     * Derives the overall order status from item statuses.
     *
     * @param items all items belonging to the order
     * @return the derived overall order status
     */
    public static OrderStatus derive(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return OrderStatus.PLACED;
        }

        Set<OrderItemStatus> statuses = items.stream()
                .map(OrderItemEntity::getStatus)
                .collect(Collectors.toSet());

        // All items share the same terminal status
        if (statuses.size() == 1) {
            OrderItemStatus single = statuses.iterator().next();
            return switch (single) {
                case DELIVERED, RETURNED -> OrderStatus.DELIVERED;
                case CANCELLED -> OrderStatus.CANCELLED;
                case REJECTED -> OrderStatus.REJECTED;
                case SHIPPED -> OrderStatus.SHIPPED;
                case PREPARING, RECEIVED -> OrderStatus.PROCESSING;
                case PLACED -> OrderStatus.PLACED;
            };
        }

        boolean hasDelivered = statuses.contains(OrderItemStatus.DELIVERED)
                || statuses.contains(OrderItemStatus.RETURNED);
        boolean allTerminal = statuses.stream().allMatch(TERMINAL_STATUSES::contains);

        // Some delivered, rest are terminal (cancelled/rejected)
        if (hasDelivered && allTerminal) {
            return OrderStatus.PARTIALLY_DELIVERED;
        }

        // Some delivered, some still active
        if (hasDelivered) {
            return OrderStatus.PARTIALLY_DELIVERED;
        }

        // Mix of terminal and active statuses
        boolean hasTerminal = statuses.stream().anyMatch(TERMINAL_STATUSES::contains);
        boolean hasActive = statuses.stream().anyMatch(s -> !TERMINAL_STATUSES.contains(s));
        if (hasTerminal && hasActive) {
            return OrderStatus.PARTIALLY_PROCESSED;
        }

        // All active: check progression
        if (statuses.contains(OrderItemStatus.SHIPPED)) {
            return OrderStatus.SHIPPED;
        }
        if (statuses.contains(OrderItemStatus.PREPARING) || statuses.contains(OrderItemStatus.RECEIVED)) {
            return OrderStatus.PROCESSING;
        }

        return OrderStatus.PLACED;
    }
}
