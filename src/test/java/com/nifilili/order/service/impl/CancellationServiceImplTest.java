package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.CancellationStatus;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.CancellationRequestEntity;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.dto.request.CancellationDecisionRequest;
import com.nifilili.order.dto.request.CancellationItemRequest;
import com.nifilili.order.dto.request.CancellationRequest;
import com.nifilili.order.dto.response.CancellationResponse;
import com.nifilili.order.events.CancellationApprovedEvent;
import com.nifilili.order.repository.CancellationRequestRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancellationServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long ORDER_ID = 10L;
    private static final Long ITEM_ID = 100L;

    @Mock
    private CancellationRequestRepository cancellationRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CancellationServiceImpl cancellationService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- requestCancellation ---

    @Test
    void requestCancellation_WhenValidRequest_ShouldCreateCancellation() {
        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED, PaymentStatus.PENDING);
        order.setUserId(USER_ID);
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PLACED);

        CancellationItemRequest itemReq = new CancellationItemRequest();
        itemReq.setOrderItemId(ITEM_ID);
        itemReq.setReason("Changed my mind");

        CancellationRequest request = new CancellationRequest();
        request.setItems(List.of(itemReq));

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(cancellationRepository.existsByOrderItemIdAndStatus(ITEM_ID, CancellationStatus.PENDING))
                .thenReturn(false);

        List<CancellationResponse> responses = cancellationService.requestCancellation(ORDER_ID, request);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("PENDING");
        verify(cancellationRepository).save(any(CancellationRequestEntity.class));
    }

    @Test
    void requestCancellation_WhenOrderNotFound_ShouldThrowResourceNotFound() {
        CancellationRequest request = new CancellationRequest();
        request.setItems(List.of());

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cancellationService.requestCancellation(ORDER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void requestCancellation_WhenOrderNotOwnedByUser_ShouldThrowResourceNotFound() {
        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED, PaymentStatus.PENDING);
        order.setUserId(999L); // Different user

        CancellationItemRequest itemReq = new CancellationItemRequest();
        itemReq.setOrderItemId(ITEM_ID);
        itemReq.setReason("Reason");

        CancellationRequest request = new CancellationRequest();
        request.setItems(List.of(itemReq));

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> cancellationService.requestCancellation(ORDER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void requestCancellation_WhenItemNotCancellable_ShouldThrowInvalidOrderState() {
        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.SHIPPED, PaymentStatus.COMPLETED);
        order.setUserId(USER_ID);
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.SHIPPED);

        CancellationItemRequest itemReq = new CancellationItemRequest();
        itemReq.setOrderItemId(ITEM_ID);
        itemReq.setReason("Want to cancel");

        CancellationRequest request = new CancellationRequest();
        request.setItems(List.of(itemReq));

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cancellationService.requestCancellation(ORDER_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    void requestCancellation_WhenDuplicatePending_ShouldThrowInvalidOrderState() {
        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED, PaymentStatus.PENDING);
        order.setUserId(USER_ID);
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PLACED);

        CancellationItemRequest itemReq = new CancellationItemRequest();
        itemReq.setOrderItemId(ITEM_ID);
        itemReq.setReason("Reason");

        CancellationRequest request = new CancellationRequest();
        request.setItems(List.of(itemReq));

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(cancellationRepository.existsByOrderItemIdAndStatus(ITEM_ID, CancellationStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> cancellationService.requestCancellation(ORDER_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already pending");
    }

    // --- decide (approve) ---

    @Test
    void decide_WhenApprovedAndPaymentCompleted_ShouldPublishCancellationEvent() {
        CancellationRequestEntity cancellation = buildCancellation(1L, ITEM_ID, CancellationStatus.PENDING);
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PLACED);
        item.setSubtotal(new BigDecimal("500.00"));
        item.setDiscountAmount(new BigDecimal("50.00"));
        item.setTaxAmount(new BigDecimal("58.50"));
        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED, PaymentStatus.COMPLETED);

        CancellationDecisionRequest request = new CancellationDecisionRequest();
        request.setApproved(true);
        request.setReason("Approved by business");

        when(cancellationRepository.findByOrderItemId(ITEM_ID)).thenReturn(Optional.of(cancellation));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        CancellationResponse response = cancellationService.decide(ITEM_ID, request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");

        // Verify item status changed to CANCELLED
        ArgumentCaptor<OrderItemEntity> itemCaptor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getStatus()).isEqualTo(OrderItemStatus.CANCELLED);

        // Verify event published with refundable amount
        ArgumentCaptor<CancellationApprovedEvent> eventCaptor =
                ArgumentCaptor.forClass(CancellationApprovedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        // refundable = subtotal - discountAmount + taxAmount = 500 - 50 + 58.50 = 508.50
        assertThat(eventCaptor.getValue().refundableAmount())
                .isEqualByComparingTo(new BigDecimal("508.50"));
    }

    @Test
    void decide_WhenApprovedAndPaymentPending_ShouldNotPublishEvent() {
        CancellationRequestEntity cancellation = buildCancellation(1L, ITEM_ID, CancellationStatus.PENDING);
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.PLACED);
        item.setSubtotal(BigDecimal.ZERO);
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setTaxAmount(BigDecimal.ZERO);
        OrderEntity order = buildOrder(ORDER_ID, OrderStatus.PLACED, PaymentStatus.PENDING);

        CancellationDecisionRequest request = new CancellationDecisionRequest();
        request.setApproved(true);

        when(cancellationRepository.findByOrderItemId(ITEM_ID)).thenReturn(Optional.of(cancellation));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        cancellationService.decide(ITEM_ID, request);

        verify(eventPublisher, never()).publishEvent(any(CancellationApprovedEvent.class));
    }

    // --- decide (reject) ---

    @Test
    void decide_WhenRejected_ShouldUpdateStatusOnly() {
        CancellationRequestEntity cancellation = buildCancellation(1L, ITEM_ID, CancellationStatus.PENDING);

        CancellationDecisionRequest request = new CancellationDecisionRequest();
        request.setApproved(false);
        request.setReason("Cannot cancel at this stage");

        when(cancellationRepository.findByOrderItemId(ITEM_ID)).thenReturn(Optional.of(cancellation));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(buildItem(ITEM_ID, OrderItemStatus.PLACED)));

        CancellationResponse response = cancellationService.decide(ITEM_ID, request);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void decide_WhenNotPending_ShouldThrowInvalidOrderState() {
        CancellationRequestEntity cancellation = buildCancellation(1L, ITEM_ID, CancellationStatus.APPROVED);

        CancellationDecisionRequest request = new CancellationDecisionRequest();
        request.setApproved(true);

        when(cancellationRepository.findByOrderItemId(ITEM_ID)).thenReturn(Optional.of(cancellation));

        assertThatThrownBy(() -> cancellationService.decide(ITEM_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("not in PENDING");
    }

    // --- helpers ---

    private OrderItemEntity buildItem(Long id, OrderItemStatus status) {
        OrderItemEntity item = OrderItemEntity.builder()
                .orderId(ORDER_ID)
                .businessId(1L)
                .status(status)
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .build();
        return TestEntityIdUtil.withId(item, id);
    }

    private OrderEntity buildOrder(Long id, OrderStatus status, PaymentStatus paymentStatus) {
        OrderEntity order = OrderEntity.builder()
                .status(status)
                .paymentStatus(paymentStatus)
                .build();
        return TestEntityIdUtil.withId(order, id);
    }

    private CancellationRequestEntity buildCancellation(Long id, Long orderItemId,
                                                         CancellationStatus status) {
        CancellationRequestEntity entity = CancellationRequestEntity.builder()
                .orderItemId(orderItemId)
                .reason("Test reason")
                .status(status)
                .build();
        return TestEntityIdUtil.withId(entity, id);
    }
}
