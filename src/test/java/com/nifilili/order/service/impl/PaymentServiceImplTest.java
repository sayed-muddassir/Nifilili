package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.request.CodPaymentRequest;
import com.nifilili.order.dto.request.VerifyPaymentRequest;
import com.nifilili.order.dto.response.PaymentStatusResponse;
import com.nifilili.order.events.PaymentCompletedEvent;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.PaymentStatusHistoryRepository;
import com.nifilili.order.repository.PaymentTypeRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long ORDER_ID = 10L;
    private static final Long PAYMENT_ID = 200L;

    @Mock
    private OrderPaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private PaymentStatusHistoryRepository paymentHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- getPayment ---

    @Test
    void getPayment_WhenPaymentExists_ShouldReturnResponse() {
        OrderPaymentEntity payment = buildPayment(PAYMENT_ID, PaymentStatus.PENDING, new BigDecimal("500.00"));
        PaymentTypeEntity paymentType = new PaymentTypeEntity();
        paymentType.setName("Cash on Delivery");

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(paymentType));

        PaymentStatusResponse response = paymentService.getPayment(ORDER_ID);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getTypeName()).isEqualTo("Cash on Delivery");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void getPayment_WhenPaymentNotFound_ShouldThrowResourceNotFound() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(ORDER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- verifyPayment ---

    @Test
    void verifyPayment_WhenApproved_ShouldSetVerifiedAndPublishEvent() {
        OrderPaymentEntity payment = buildPayment(PAYMENT_ID, PaymentStatus.PENDING_VERIFICATION,
                new BigDecimal("500.00"));
        OrderEntity order = buildOrder(ORDER_ID);

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setVerified(true);
        request.setReference("TXN-12345");

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        paymentService.verifyPayment(ORDER_ID, request);

        ArgumentCaptor<OrderPaymentEntity> payCaptor = ArgumentCaptor.forClass(OrderPaymentEntity.class);
        verify(paymentRepository).save(payCaptor.capture());
        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.VERIFIED);

        ArgumentCaptor<PaymentCompletedEvent> eventCaptor =
                ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(ORDER_ID);
    }

    @Test
    void verifyPayment_WhenRejected_ShouldSetFailedAndNotPublishEvent() {
        OrderPaymentEntity payment = buildPayment(PAYMENT_ID, PaymentStatus.PENDING_VERIFICATION,
                new BigDecimal("500.00"));
        OrderEntity order = buildOrder(ORDER_ID);

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setVerified(false);

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        paymentService.verifyPayment(ORDER_ID, request);

        ArgumentCaptor<OrderPaymentEntity> captor = ArgumentCaptor.forClass(OrderPaymentEntity.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(eventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    void verifyPayment_WhenNotPendingVerification_ShouldThrowInvalidOrderState() {
        OrderPaymentEntity payment = buildPayment(PAYMENT_ID, PaymentStatus.COMPLETED,
                new BigDecimal("500.00"));

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setVerified(true);

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.verifyPayment(ORDER_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("PENDING_VERIFICATION");
    }

    // --- recordCodPayment ---

    @Test
    void recordCodPayment_WhenPending_ShouldCompleteAndPublishEvent() {
        OrderPaymentEntity payment = buildPayment(PAYMENT_ID, PaymentStatus.PENDING,
                new BigDecimal("500.00"));
        OrderEntity order = buildOrder(ORDER_ID);

        CodPaymentRequest request = new CodPaymentRequest();
        request.setAmountReceived(new BigDecimal("500.00"));
        request.setReceiverName("Delivery Agent");

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        paymentService.recordCodPayment(ORDER_ID, request);

        // Payment should be COMPLETED
        ArgumentCaptor<OrderPaymentEntity> payCaptor = ArgumentCaptor.forClass(OrderPaymentEntity.class);
        verify(paymentRepository).save(payCaptor.capture());
        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        // Order should have amountPaid updated
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAmountPaid())
                .isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(orderCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);

        // Event should be published
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    void recordCodPayment_WhenNotPending_ShouldThrowInvalidOrderState() {
        OrderPaymentEntity payment = buildPayment(PAYMENT_ID, PaymentStatus.COMPLETED,
                new BigDecimal("500.00"));

        CodPaymentRequest request = new CodPaymentRequest();
        request.setAmountReceived(new BigDecimal("500.00"));

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.recordCodPayment(ORDER_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("PENDING");
    }

    // --- helpers ---

    private OrderPaymentEntity buildPayment(Long id, PaymentStatus status, BigDecimal amount) {
        OrderPaymentEntity payment = OrderPaymentEntity.builder()
                .orderId(ORDER_ID)
                .paymentTypeId(1L)
                .amount(amount)
                .status(status)
                .paymentDetails(Map.of())
                .build();
        return TestEntityIdUtil.withId(payment, id);
    }

    private OrderEntity buildOrder(Long id) {
        OrderEntity order = OrderEntity.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .amountPaid(BigDecimal.ZERO)
                .build();
        return TestEntityIdUtil.withId(order, id);
    }
}
