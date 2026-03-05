package com.nifilili.order.util;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.order.domain.OrderItemEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatusDeriverTest {

    @Test
    void derive_WhenAllDelivered_ShouldReturnDelivered() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.DELIVERED),
                itemWithStatus(OrderItemStatus.DELIVERED)
        );
        assertEquals(OrderStatus.DELIVERED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenAllCancelled_ShouldReturnCancelled() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.CANCELLED),
                itemWithStatus(OrderItemStatus.CANCELLED)
        );
        assertEquals(OrderStatus.CANCELLED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenAllRejected_ShouldReturnRejected() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.REJECTED),
                itemWithStatus(OrderItemStatus.REJECTED)
        );
        assertEquals(OrderStatus.REJECTED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenMixedDeliveredAndCancelled_ShouldReturnPartiallyDelivered() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.DELIVERED),
                itemWithStatus(OrderItemStatus.CANCELLED)
        );
        assertEquals(OrderStatus.PARTIALLY_DELIVERED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenSomeDeliveredSomeShipped_ShouldReturnPartiallyDelivered() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.DELIVERED),
                itemWithStatus(OrderItemStatus.SHIPPED)
        );
        assertEquals(OrderStatus.PARTIALLY_DELIVERED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenAllShipped_ShouldReturnShipped() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.SHIPPED),
                itemWithStatus(OrderItemStatus.SHIPPED)
        );
        assertEquals(OrderStatus.SHIPPED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenAllPlaced_ShouldReturnPlaced() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.PLACED),
                itemWithStatus(OrderItemStatus.PLACED)
        );
        assertEquals(OrderStatus.PLACED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenSomePreparing_ShouldReturnProcessing() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.PLACED),
                itemWithStatus(OrderItemStatus.PREPARING)
        );
        assertEquals(OrderStatus.PROCESSING, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenMixedTerminalAndActive_ShouldReturnPartiallyProcessed() {
        List<OrderItemEntity> items = List.of(
                itemWithStatus(OrderItemStatus.CANCELLED),
                itemWithStatus(OrderItemStatus.PLACED)
        );
        assertEquals(OrderStatus.PARTIALLY_PROCESSED, OrderStatusDeriver.derive(items));
    }

    @Test
    void derive_WhenEmptyList_ShouldReturnPlaced() {
        assertEquals(OrderStatus.PLACED, OrderStatusDeriver.derive(List.of()));
    }

    @Test
    void derive_WhenNull_ShouldReturnPlaced() {
        assertEquals(OrderStatus.PLACED, OrderStatusDeriver.derive(null));
    }

    private OrderItemEntity itemWithStatus(OrderItemStatus status) {
        return OrderItemEntity.builder().status(status).build();
    }
}
