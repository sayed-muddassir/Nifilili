package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.dto.request.RejectOrderItemRequest;
import com.nifilili.order.dto.request.UpdateOrderItemStatusRequest;
import com.nifilili.order.events.OrderItemStatusChangedEvent;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemLifecycleServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long ORDER_ID = 10L;
    private static final Long ITEM_ID = 100L;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderItemLifecycleServiceImpl lifecycleService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- updateStatus ---

    @Test
    void updateStatus_WhenValidTransition_ShouldUpdateAndPublishEvent() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PLACED);
        UpdateOrderItemStatusRequest request = new UpdateOrderItemStatusRequest();
        request.setNewStatus("RECEIVED");
        request.setNote("Received at warehouse");

        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED);

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        lifecycleService.updateStatus(ITEM_ID, request);

        // Verify item was saved with new status
        ArgumentCaptor<OrderItemEntity> itemCaptor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getStatus()).isEqualTo(OrderItemStatus.RECEIVED);

        // Verify history was recorded
        ArgumentCaptor<OrderStatusHistoryEntity> histCaptor =
                ArgumentCaptor.forClass(OrderStatusHistoryEntity.class);
        verify(statusHistoryRepository).save(histCaptor.capture());
        assertThat(histCaptor.getValue().getOldStatus()).isEqualTo("PLACED");
        assertThat(histCaptor.getValue().getNewStatus()).isEqualTo("RECEIVED");

        // Verify event was published
        ArgumentCaptor<OrderItemStatusChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderItemStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().oldStatus()).isEqualTo(OrderItemStatus.PLACED);
        assertThat(eventCaptor.getValue().newStatus()).isEqualTo(OrderItemStatus.RECEIVED);
    }

    @Test
    void updateStatus_WhenInvalidTransition_ShouldThrowInvalidOrderState() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.DELIVERED);
        UpdateOrderItemStatusRequest request = new UpdateOrderItemStatusRequest();
        request.setNewStatus("PLACED");

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> lifecycleService.updateStatus(ITEM_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    void updateStatus_WhenItemNotFound_ShouldThrowResourceNotFound() {
        UpdateOrderItemStatusRequest request = new UpdateOrderItemStatusRequest();
        request.setNewStatus("RECEIVED");

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lifecycleService.updateStatus(ITEM_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_WhenPreparingToShipped_ShouldBeValid() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PREPARING);
        UpdateOrderItemStatusRequest request = new UpdateOrderItemStatusRequest();
        request.setNewStatus("SHIPPED");

        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PROCESSING);

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        lifecycleService.updateStatus(ITEM_ID, request);

        ArgumentCaptor<OrderItemEntity> captor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderItemStatus.SHIPPED);
    }

    // --- reject ---

    @Test
    void reject_WhenPlacedStatus_ShouldSetRejected() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PLACED);
        RejectOrderItemRequest request = new RejectOrderItemRequest();
        request.setReason("Out of stock");

        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED);

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        lifecycleService.reject(ITEM_ID, request);

        ArgumentCaptor<OrderItemEntity> captor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderItemStatus.REJECTED);
    }

    @Test
    void reject_WhenReceivedStatus_ShouldSetRejected() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.RECEIVED);
        RejectOrderItemRequest request = new RejectOrderItemRequest();
        request.setReason("Quality issue");

        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED);

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        lifecycleService.reject(ITEM_ID, request);

        ArgumentCaptor<OrderItemEntity> captor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderItemStatus.REJECTED);
    }

    @Test
    void reject_WhenShippedStatus_ShouldThrowInvalidOrderState() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.SHIPPED);
        RejectOrderItemRequest request = new RejectOrderItemRequest();
        request.setReason("Some reason");

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> lifecycleService.reject(ITEM_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot reject");
    }

    @Test
    void reject_WhenItemNotFound_ShouldThrowResourceNotFound() {
        RejectOrderItemRequest request = new RejectOrderItemRequest();
        request.setReason("Some reason");

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lifecycleService.reject(ITEM_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- helpers ---

    private OrderItemEntity buildItem(Long id, OrderItemStatus status) {
        OrderItemEntity item = OrderItemEntity.builder()
                .orderId(ORDER_ID)
                .businessId(1L)
                .offeringId(200L)
                .status(status)
                .build();
        return TestEntityIdUtil.withId(item, id);
    }

    private OrderEntity buildOrder(Long id, OrderStatus status) {
        OrderEntity order = OrderEntity.builder()
                .status(status)
                .build();
        return TestEntityIdUtil.withId(order, id);
    }
}
