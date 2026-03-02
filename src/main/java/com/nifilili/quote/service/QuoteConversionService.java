package com.nifilili.quote.service;

import com.nifilili.quote.domain.Quote;
import com.nifilili.quote.domain.QuoteConversion;
import com.nifilili.quote.domain.QuoteLineItem;
import com.nifilili.quote.domain.QuoteRequest;
import com.nifilili.quote.repository.QuoteConversionRepository;
import com.nifilili.quote.repository.QuoteLineItemRepository;
import com.nifilili.quote.repository.QuoteRepository;
import com.nifilili.quote.repository.QuoteRequestRepository;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteConversionService {

    private final QuoteRepository quoteRepository;
    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteLineItemRepository quoteLineItemRepository;
    private final QuoteConversionRepository quoteConversionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional
    public Long convertToOrder(Long quoteId, Long userId) {
        if (quoteConversionRepository.existsByQuoteId(quoteId)) {
            return quoteConversionRepository.findByQuoteId(quoteId)
                    .map(QuoteConversion::getOrderId)
                    .orElseThrow(() -> new IllegalStateException("Quote conversion record is corrupted"));
        }

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        QuoteRequest request = quoteRequestRepository.findById(quote.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        OrderEntity order = createOrderFromQuote(quote, request, userId);
        OrderEntity savedOrder = orderRepository.save(order);

        createOrderItems(savedOrder, quote, request, userId);
        saveConversion(quoteId, savedOrder.getId(), userId);

        return savedOrder.getId();
    }

    private OrderEntity createOrderFromQuote(Quote quote, QuoteRequest request, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        OrderEntity order = new OrderEntity();
        order.setUserId(request.getUserId());
        order.setOrderNumber(generateOrderNumber());
        order.setInvoiceNumber(generateInvoiceNumber());

        order.setReceiverName("Quote Customer");
        order.setContactNumber("N/A");
        order.setEmail("quote-customer@nifilili.local");

        order.setMunicipalityId(asLongOrDefault(request.getDeliveryAddress(), "municipalityId", 0L));
        order.setWardNumber(asIntegerOrDefault(request.getDeliveryAddress(), "wardNumber", 0));
        order.setToleName(asStringOrDefault(request.getDeliveryAddress(), "toleName", "N/A"));
        order.setAddressField1(asStringOrDefault(request.getDeliveryAddress(), "addressField1", "N/A"));
        order.setPostalCode(asStringOrDefault(request.getDeliveryAddress(), "postalCode", "00000"));

        order.setSubtotalAmount(quote.getTotalAmount());
        order.setDeliveryCharge(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(quote.getTotalAmount());

        order.setStatus("placed");
        order.setPaymentStatus("pending");
        order.setAmountPaid(BigDecimal.ZERO);
        order.setCustomerNotes("Created from quote " + quote.getQuoteNumber());
        order.setIsB2bOrder(false);
        order.setB2bQuoteId(0L);
        order.setSource("quote");
        order.setSourceQuoteId(quote.getId());
        order.setSourceRequestId(request.getId());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setCreatedBy(userId);
        order.setUpdatedBy(userId);

        return order;
    }

    private void createOrderItems(OrderEntity order, Quote quote, QuoteRequest request, Long userId) {
        List<QuoteLineItem> lineItems = quoteLineItemRepository.findByQuoteId(quote.getId());
        LocalDateTime now = LocalDateTime.now();

        if (lineItems.isEmpty()) {
            OrderItemEntity item = buildOrderItem(
                    order, request.getBusinessId(), request.getOfferingId(),
                    quote.getServiceDetails(), quote.getQuoteNumber(),
                    1, quote.getTotalAmount(), now, userId
            );
            OrderItemEntity saved = orderItemRepository.save(item);
            createPlacedHistory(saved.getId(), now, userId);
            return;
        }

        for (QuoteLineItem lineItem : lineItems) {
            OrderItemEntity item = buildOrderItem(
                    order, request.getBusinessId(), request.getOfferingId(),
                    lineItem.getDescription(), quote.getQuoteNumber(),
                    lineItem.getQuantity(), lineItem.getUnitPrice(), now, userId
            );
            OrderItemEntity saved = orderItemRepository.save(item);
            createPlacedHistory(saved.getId(), now, userId);
        }
    }

    private OrderItemEntity buildOrderItem(
            OrderEntity order,
            Long businessId,
            Long offeringId,
            String title,
            String sku,
            Integer quantity,
            BigDecimal unitPrice,
            LocalDateTime now,
            Long userId
    ) {
        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setBusinessId(businessId);
        item.setOfferingId(offeringId);
        item.setVariantId(null);
        item.setCouponId(null);
        item.setTitle(title == null ? "Quoted Service" : title);
        item.setSku(sku);
        item.setVariantAttributes(Map.of());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setDeliveryCharge(BigDecimal.ZERO);
        item.setTaxAmount(BigDecimal.ZERO);
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        item.setStatus("placed");
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item.setCreatedBy(userId);
        item.setUpdatedBy(userId);
        return item;
    }

    private void createPlacedHistory(Long orderItemId, LocalDateTime now, Long userId) {
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrderItemId(orderItemId);
        history.setOldStatus("created");
        history.setNewStatus("placed");
        history.setRejectionReason("NA");
        history.setCreatedAt(now);
        history.setCreatedBy(userId);
        orderStatusHistoryRepository.save(history);
    }

    private void saveConversion(Long quoteId, Long orderId, Long userId) {
        QuoteConversion conversion = new QuoteConversion();
        conversion.setQuoteId(quoteId);
        conversion.setOrderId(orderId);
        conversion.setConvertedAt(LocalDateTime.now());
        conversion.setConvertedBy(userId);
        quoteConversionRepository.save(conversion);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long asLongOrDefault(Map<String, Object> map, String key, Long fallback) {
        if (map == null || map.get(key) == null) {
            return fallback;
        }
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Integer asIntegerOrDefault(Map<String, Object> map, String key, Integer fallback) {
        if (map == null || map.get(key) == null) {
            return fallback;
        }
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String asStringOrDefault(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) {
            return fallback;
        }
        String value = String.valueOf(map.get(key));
        return value.isBlank() ? fallback : value;
    }
}
