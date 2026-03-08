package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.events.OrderCreatedFromQuoteEvent;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.quote.events.QuoteAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for QuoteAcceptedEvent from the quote module and creates
 * an order with all quote details snapshotted into the order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteAcceptedEventListener {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void onQuoteAccepted(QuoteAcceptedEvent event) {
        log.info("Creating order from accepted quote: quoteId={}, userId={}", event.quoteId(), event.userId());

        LocalDateTime now = LocalDateTime.now();
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Extract delivery address with defaults
        Map<String, Object> addr = event.deliveryAddress();
        String receiverName = getStringOrDefault(addr, "receiverName", "N/A");
        String contactNumber = getStringOrDefault(addr, "contactNumber", "N/A");
        String email = getStringOrDefault(addr, "email", "N/A");
        Long municipalityId = getLongOrDefault(addr, "municipalityId", 0L);
        Integer wardNumber = getIntOrDefault(addr, "wardNumber", 0);
        String toleName = getStringOrDefault(addr, "toleName", "N/A");
        String addressField1 = getStringOrDefault(addr, "addressField1", "N/A");
        String postalCode = getStringOrDefault(addr, "postalCode", "00000");

        OrderEntity order = OrderEntity.builder()
                .userId(event.userId())
                .orderNumber(orderNumber)
                .invoiceNumber(invoiceNumber)
                .receiverName(receiverName)
                .contactNumber(contactNumber)
                .email(email)
                .municipalityId(municipalityId)
                .wardNumber(wardNumber)
                .toleName(toleName)
                .addressField1(addressField1)
                .postalCode(postalCode)
                .subtotalAmount(event.totalAmount())
                .deliveryCharge(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(event.totalAmount())
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .amountPaid(BigDecimal.ZERO)
                .source("quote")
                .sourceQuoteId(event.quoteId())
                .sourceRequestId(event.requestId())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(event.userId())
                .updatedBy(event.userId())
                .build();

        order = orderRepository.save(order);
        log.info("Order created from quote: orderId={}, orderNumber={}", order.getId(), orderNumber);

        // Create order items from line items
        for (QuoteAcceptedEvent.LineItemData li : event.lineItems()) {
            OrderItemEntity item = OrderItemEntity.builder()
                    .orderId(order.getId())
                    .businessId(event.businessId())
                    .offeringId(event.offeringId())
                    .title(li.description())
                    .quantity(li.quantity())
                    .unitPrice(li.unitPrice())
                    .discountAmount(BigDecimal.ZERO)
                    .deliveryCharge(BigDecimal.ZERO)
                    .taxAmount(BigDecimal.ZERO)
                    .subtotal(li.totalPrice())
                    .status(OrderItemStatus.PLACED)
                    .createdAt(now)
                    .updatedAt(now)
                    .createdBy(event.userId())
                    .updatedBy(event.userId())
                    .build();
            orderItemRepository.save(item);
        }

        // Record status history
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.builder()
                .orderItemId(order.getId())
                .oldStatus("NONE")
                .newStatus(OrderStatus.PLACED.name())
                .createdBy(event.userId())
                .createdAt(now)
                .build();
        orderStatusHistoryRepository.save(history);

        // Publish event back so quote module records the conversion
        eventPublisher.publishEvent(new OrderCreatedFromQuoteEvent(
                event.quoteId(), order.getId(), orderNumber, event.userId()));
    }

    private String getStringOrDefault(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        return String.valueOf(map.get(key));
    }

    private Long getLongOrDefault(Map<String, Object> map, String key, Long defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        Object val = map.get(key);
        if (val instanceof Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    private Integer getIntOrDefault(Map<String, Object> map, String key, Integer defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        Object val = map.get(key);
        if (val instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }
}
