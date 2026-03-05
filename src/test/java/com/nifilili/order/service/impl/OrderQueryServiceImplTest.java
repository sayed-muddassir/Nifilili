package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.response.OrderDetailsResponse;
import com.nifilili.order.dto.response.OrderItemDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import com.nifilili.order.mapper.OrderMapper;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.repository.PaymentTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long ORDER_ID = 10L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private OrderPaymentRepository orderPaymentRepository;

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- listMyOrders ---

    @Test
    void listMyOrders_WhenNoStatusFilter_ShouldReturnAllOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderEntity order = buildOrder(ORDER_ID);
        OrderSummaryResponse summary = OrderSummaryResponse.builder()
                .orderId(ORDER_ID).orderNumber("ORD-12345").status("PLACED").build();

        when(orderRepository.findByUserId(USER_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(orderMapper.toSummary(order)).thenReturn(summary);

        Page<OrderSummaryResponse> result = queryService.listMyOrders(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderNumber()).isEqualTo("ORD-12345");
        verify(orderRepository).findByUserId(USER_ID, pageable);
    }

    @Test
    void listMyOrders_WhenStatusFilter_ShouldFilterByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderEntity order = buildOrder(ORDER_ID);
        OrderSummaryResponse summary = OrderSummaryResponse.builder()
                .orderId(ORDER_ID).status("DELIVERED").build();

        when(orderRepository.findByUserIdAndStatus(USER_ID, OrderStatus.DELIVERED, pageable))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(orderMapper.toSummary(order)).thenReturn(summary);

        Page<OrderSummaryResponse> result = queryService.listMyOrders(OrderStatus.DELIVERED, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByUserIdAndStatus(USER_ID, OrderStatus.DELIVERED, pageable);
    }

    // --- getOrderDetails ---

    @Test
    void getOrderDetails_WhenValidOrder_ShouldReturnFullDetails() {
        OrderEntity order = buildOrder(ORDER_ID);
        OrderItemEntity item = buildItem(100L, ORDER_ID);
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.builder()
                .orderItemId(100L).oldStatus("PLACED").newStatus("RECEIVED")
                .createdAt(LocalDateTime.now()).build();
        OrderPaymentEntity payment = OrderPaymentEntity.builder()
                .orderId(ORDER_ID).paymentTypeId(1L).amount(new BigDecimal("226.00"))
                .status(PaymentStatus.VERIFIED).build();
        PaymentTypeEntity paymentType = new PaymentTypeEntity();
        paymentType.setName("Manual Bank Transfer");
        OrderItemDetailsResponse itemDetails = OrderItemDetailsResponse.builder()
                .orderItemId(100L).title("Product").build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderMapper.toItemDetails(item)).thenReturn(itemDetails);
        when(statusHistoryRepository.findByOrderItemIdOrderByCreatedAtAsc(100L))
                .thenReturn(List.of(history));
        when(orderPaymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(paymentType));

        OrderDetailsResponse response = queryService.getOrderDetails(ORDER_ID);

        assertThat(response.getOrderNumber()).isEqualTo("ORD-12345");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getTimeline()).hasSize(1);
        assertThat(response.getPayment()).isNotNull();
        assertThat(response.getPayment().getPaymentType()).isEqualTo("Manual Bank Transfer");
    }

    @Test
    void getOrderDetails_WhenOrderNotFound_ShouldThrowResourceNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getOrderDetails(ORDER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrderDetails_WhenOrderOwnedByDifferentUser_ShouldThrowResourceNotFound() {
        OrderEntity order = buildOrder(ORDER_ID);
        order.setUserId(999L); // Different user

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> queryService.getOrderDetails(ORDER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrderDetails_WhenNoPayment_ShouldReturnNullPayment() {
        OrderEntity order = buildOrder(ORDER_ID);
        OrderItemEntity item = buildItem(100L, ORDER_ID);
        OrderItemDetailsResponse itemDetails = OrderItemDetailsResponse.builder()
                .orderItemId(100L).title("Product").build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(item));
        when(orderMapper.toItemDetails(item)).thenReturn(itemDetails);
        when(statusHistoryRepository.findByOrderItemIdOrderByCreatedAtAsc(100L))
                .thenReturn(List.of());
        when(orderPaymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        OrderDetailsResponse response = queryService.getOrderDetails(ORDER_ID);

        assertThat(response.getPayment()).isNull();
    }

    // --- helpers ---

    private OrderEntity buildOrder(Long id) {
        OrderEntity order = OrderEntity.builder()
                .userId(USER_ID)
                .orderNumber("ORD-12345")
                .status(OrderStatus.PLACED)
                .receiverName("John Doe")
                .contactNumber("9841234567")
                .addressField1("123 Main St")
                .wardNumber(5)
                .toleName("Kathmandu")
                .subtotalAmount(new BigDecimal("200.00"))
                .deliveryCharge(BigDecimal.ZERO)
                .taxAmount(new BigDecimal("26.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("226.00"))
                .createdAt(LocalDateTime.now())
                .build();
        return TestEntityIdUtil.withId(order, id);
    }

    private OrderItemEntity buildItem(Long id, Long orderId) {
        OrderItemEntity item = OrderItemEntity.builder()
                .orderId(orderId)
                .businessId(5L)
                .title("Product")
                .status(OrderItemStatus.PLACED)
                .build();
        return TestEntityIdUtil.withId(item, id);
    }
}
