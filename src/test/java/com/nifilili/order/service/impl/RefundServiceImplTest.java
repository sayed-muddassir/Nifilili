package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.RefundStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderRefundEntity;
import com.nifilili.order.dto.request.CreateRefundRequest;
import com.nifilili.order.dto.request.UpdateRefundStatusRequest;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.events.RefundProcessedEvent;
import com.nifilili.order.mapper.RefundMapper;
import com.nifilili.order.repository.OrderRefundRepository;
import com.nifilili.order.repository.OrderRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long ORDER_ID = 10L;
    private static final Long REFUND_ID = 300L;

    @Mock
    private OrderRefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RefundMapper refundMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RefundServiceImpl refundService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- createRefund ---

    @Test
    void createRefund_WhenValidRequest_ShouldCreateRefund() {
        OrderEntity order = TestEntityIdUtil.withId(new OrderEntity(), ORDER_ID);
        CreateRefundRequest request = buildCreateRequest();
        OrderRefundEntity saved = buildRefund(REFUND_ID, RefundStatus.PENDING);
        RefundResponse expectedResponse = RefundResponse.builder()
                .refundId(REFUND_ID).status("PENDING").build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(refundRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());
        when(refundRepository.save(any(OrderRefundEntity.class))).thenReturn(saved);
        when(refundMapper.toResponse(saved)).thenReturn(expectedResponse);

        RefundResponse response = refundService.createRefund(ORDER_ID, request);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        ArgumentCaptor<OrderRefundEntity> captor = ArgumentCaptor.forClass(OrderRefundEntity.class);
        verify(refundRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().getBankName()).isEqualTo("Nepal Bank");
    }

    @Test
    void createRefund_WhenOrderNotFound_ShouldThrowResourceNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refundService.createRefund(ORDER_ID, buildCreateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createRefund_WhenRefundAlreadyExists_ShouldThrowInvalidOrderState() {
        OrderEntity order = TestEntityIdUtil.withId(new OrderEntity(), ORDER_ID);
        OrderRefundEntity existing = buildRefund(REFUND_ID, RefundStatus.PENDING);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(refundRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> refundService.createRefund(ORDER_ID, buildCreateRequest()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already exists");
    }

    // --- updateRefundStatus ---

    @Test
    void updateRefundStatus_WhenPendingToApproved_ShouldUpdateStatus() {
        OrderRefundEntity refund = buildRefund(REFUND_ID, RefundStatus.PENDING);
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus("APPROVED");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(refundRepository.save(any(OrderRefundEntity.class))).thenReturn(refund);
        when(refundMapper.toResponse(refund)).thenReturn(
                RefundResponse.builder().status("APPROVED").build());

        RefundResponse response = refundService.updateRefundStatus(REFUND_ID, request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        verify(eventPublisher, never()).publishEvent(any(RefundProcessedEvent.class));
    }

    @Test
    void updateRefundStatus_WhenApprovedToProcessed_ShouldPublishEvent() {
        OrderRefundEntity refund = buildRefund(REFUND_ID, RefundStatus.APPROVED);
        refund.setOrderId(ORDER_ID);
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus("PROCESSED");
        request.setReference("REF-12345");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(refundRepository.save(any(OrderRefundEntity.class))).thenReturn(refund);
        when(refundMapper.toResponse(refund)).thenReturn(
                RefundResponse.builder().status("PROCESSED").build());

        refundService.updateRefundStatus(REFUND_ID, request);

        assertThat(refund.getProcessedAt()).isNotNull();
        assertThat(refund.getRefundReference()).isEqualTo("REF-12345");

        ArgumentCaptor<RefundProcessedEvent> eventCaptor =
                ArgumentCaptor.forClass(RefundProcessedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(ORDER_ID);
        assertThat(eventCaptor.getValue().refundId()).isEqualTo(REFUND_ID);
    }

    @Test
    void updateRefundStatus_WhenInvalidTransition_ShouldThrowInvalidOrderState() {
        OrderRefundEntity refund = buildRefund(REFUND_ID, RefundStatus.PENDING);
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus("PROCESSED"); // PENDING → PROCESSED not allowed

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));

        assertThatThrownBy(() -> refundService.updateRefundStatus(REFUND_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    void updateRefundStatus_WhenRefundNotFound_ShouldThrowResourceNotFound() {
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus("APPROVED");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refundService.updateRefundStatus(REFUND_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRefundStatus_WhenApprovedToFailed_ShouldUpdateWithoutEvent() {
        OrderRefundEntity refund = buildRefund(REFUND_ID, RefundStatus.APPROVED);
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus("FAILED");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(refundRepository.save(any(OrderRefundEntity.class))).thenReturn(refund);
        when(refundMapper.toResponse(refund)).thenReturn(
                RefundResponse.builder().status("FAILED").build());

        RefundResponse response = refundService.updateRefundStatus(REFUND_ID, request);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        verify(eventPublisher, never()).publishEvent(any(RefundProcessedEvent.class));
    }

    // --- helpers ---

    private OrderRefundEntity buildRefund(Long id, RefundStatus status) {
        OrderRefundEntity entity = OrderRefundEntity.builder()
                .orderId(ORDER_ID)
                .refundAmount(BigDecimal.ZERO)
                .status(status)
                .bankName("Nepal Bank")
                .bankAccountName("Test User")
                .accountNumber("1234567890")
                .branch("Main Branch")
                .build();
        return TestEntityIdUtil.withId(entity, id);
    }

    private CreateRefundRequest buildCreateRequest() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setBankName("Nepal Bank");
        request.setAccountHolder("Test User");
        request.setAccountNumber("1234567890");
        request.setBranch("Main Branch");
        return request;
    }
}
