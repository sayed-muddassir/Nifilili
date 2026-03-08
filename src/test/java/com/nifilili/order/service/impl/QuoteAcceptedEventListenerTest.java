package com.nifilili.order.service.impl;

import com.nifilili.business.TestEntityIdUtil;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteAcceptedEventListenerTest {

    private static final Long QUOTE_ID = 10L;
    private static final Long REQUEST_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Long BUSINESS_ID = 100L;
    private static final Long OFFERING_ID = 200L;
    private static final Long ORDER_ID = 500L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuoteAcceptedEventListener listener;

    @Test
    void onQuoteAccepted_WhenValidEvent_ShouldCreateOrderAndPublishEvent() {
        QuoteAcceptedEvent event = buildEvent(Map.of(
                "receiverName", "John Doe",
                "contactNumber", "9801234567",
                "email", "john@example.com",
                "municipalityId", 1,
                "wardNumber", 5,
                "toleName", "Kathmandu",
                "addressField1", "Street 1",
                "postalCode", "44600"
        ));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity order = inv.getArgument(0);
            return TestEntityIdUtil.withId(order, ORDER_ID);
        });
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistoryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.onQuoteAccepted(event);

        // Verify order created
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUserId()).isEqualTo(USER_ID);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(savedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(savedOrder.getSource()).isEqualTo("quote");
        assertThat(savedOrder.getSourceQuoteId()).isEqualTo(QUOTE_ID);
        assertThat(savedOrder.getSourceRequestId()).isEqualTo(REQUEST_ID);
        assertThat(savedOrder.getOrderNumber()).startsWith("ORD-");
        assertThat(savedOrder.getInvoiceNumber()).startsWith("INV-");
        assertThat(savedOrder.getReceiverName()).isEqualTo("John Doe");
        assertThat(savedOrder.getContactNumber()).isEqualTo("9801234567");
        assertThat(savedOrder.getEmail()).isEqualTo("john@example.com");

        // Verify order items created (2 line items)
        ArgumentCaptor<OrderItemEntity> itemCaptor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository, times(2)).save(itemCaptor.capture());
        List<OrderItemEntity> savedItems = itemCaptor.getAllValues();
        assertThat(savedItems).hasSize(2);
        assertThat(savedItems.get(0).getBusinessId()).isEqualTo(BUSINESS_ID);
        assertThat(savedItems.get(0).getOfferingId()).isEqualTo(OFFERING_ID);
        assertThat(savedItems.get(0).getTitle()).isEqualTo("Design phase");
        assertThat(savedItems.get(0).getStatus()).isEqualTo(OrderItemStatus.PLACED);
        assertThat(savedItems.get(1).getTitle()).isEqualTo("Development phase");

        // Verify status history recorded
        ArgumentCaptor<OrderStatusHistoryEntity> historyCaptor =
                ArgumentCaptor.forClass(OrderStatusHistoryEntity.class);
        verify(orderStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOldStatus()).isEqualTo("NONE");
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo("PLACED");

        // Verify event published back
        ArgumentCaptor<OrderCreatedFromQuoteEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedFromQuoteEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().quoteId()).isEqualTo(QUOTE_ID);
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(ORDER_ID);
        assertThat(eventCaptor.getValue().convertedBy()).isEqualTo(USER_ID);
    }

    @Test
    void onQuoteAccepted_WhenNullDeliveryAddress_ShouldUseDefaults() {
        QuoteAcceptedEvent event = buildEvent(null);

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity order = inv.getArgument(0);
            return TestEntityIdUtil.withId(order, ORDER_ID);
        });
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistoryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.onQuoteAccepted(event);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getReceiverName()).isEqualTo("N/A");
        assertThat(savedOrder.getContactNumber()).isEqualTo("N/A");
        assertThat(savedOrder.getEmail()).isEqualTo("N/A");
        assertThat(savedOrder.getMunicipalityId()).isEqualTo(0L);
        assertThat(savedOrder.getWardNumber()).isEqualTo(0);
        assertThat(savedOrder.getToleName()).isEqualTo("N/A");
        assertThat(savedOrder.getAddressField1()).isEqualTo("N/A");
        assertThat(savedOrder.getPostalCode()).isEqualTo("00000");
    }

    @Test
    void onQuoteAccepted_WhenPartialAddress_ShouldDefaultMissingFields() {
        QuoteAcceptedEvent event = buildEvent(Map.of(
                "receiverName", "Jane",
                "email", "jane@test.com"
        ));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity order = inv.getArgument(0);
            return TestEntityIdUtil.withId(order, ORDER_ID);
        });
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistoryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.onQuoteAccepted(event);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getReceiverName()).isEqualTo("Jane");
        assertThat(savedOrder.getEmail()).isEqualTo("jane@test.com");
        assertThat(savedOrder.getContactNumber()).isEqualTo("N/A");
        assertThat(savedOrder.getMunicipalityId()).isEqualTo(0L);
    }

    @Test
    void onQuoteAccepted_WhenNonNumberMunicipalityId_ShouldUseDefault() {
        QuoteAcceptedEvent event = buildEvent(Map.of(
                "receiverName", "Test",
                "municipalityId", "not-a-number"
        ));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity order = inv.getArgument(0);
            return TestEntityIdUtil.withId(order, ORDER_ID);
        });
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistoryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.onQuoteAccepted(event);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getMunicipalityId()).isEqualTo(0L);
        assertThat(orderCaptor.getValue().getWardNumber()).isEqualTo(0);
    }

    private QuoteAcceptedEvent buildEvent(Map<String, Object> deliveryAddress) {
        return new QuoteAcceptedEvent(
                QUOTE_ID,
                REQUEST_ID,
                USER_ID,
                BUSINESS_ID,
                OFFERING_ID,
                "QTE-TEST1234",
                "Website development",
                new BigDecimal("1500.00"),
                "NPR",
                deliveryAddress,
                List.of(
                        new QuoteAcceptedEvent.LineItemData("Design phase", 1,
                                new BigDecimal("500.00"), new BigDecimal("500.00")),
                        new QuoteAcceptedEvent.LineItemData("Development phase", 1,
                                new BigDecimal("1000.00"), new BigDecimal("1000.00"))
                )
        );
    }
}
