package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.ReturnStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.request.UpdateReturnStatusRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.events.ReturnApprovedEvent;
import com.nifilili.order.mapper.ReturnMapper;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.ReturnRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessReturnServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long RETURN_ID = 200L;
    private static final Long ITEM_ID = 100L;
    private static final Long ORDER_ID = 10L;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ReturnMapper returnMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BusinessReturnServiceImpl businessReturnService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- updateStatus: REQUESTED → PICKUP_SCHEDULED ---

    @Test
    void updateStatus_WhenRequestedToPickupScheduled_ShouldUpdateStatus() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.REQUESTED);
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("PICKUP_SCHEDULED");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));
        when(returnRequestRepository.save(any(ReturnRequestEntity.class))).thenReturn(returnReq);
        when(returnMapper.toResponse(any())).thenReturn(
                ReturnResponse.builder().status("PICKUP_SCHEDULED").build());

        ReturnResponse response = businessReturnService.updateStatus(RETURN_ID, request);

        assertThat(response.getStatus()).isEqualTo("PICKUP_SCHEDULED");
        assertThat(returnReq.getPickupScheduledAt()).isNotNull();
    }

    // --- updateStatus: INSPECTED → REFUNDED ---

    @Test
    void updateStatus_WhenInspectedToRefunded_ShouldUpdateItemAndPublishEvent() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.INSPECTED);
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.DELIVERED);

        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("REFUNDED");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(returnRequestRepository.save(any(ReturnRequestEntity.class))).thenReturn(returnReq);
        when(returnMapper.toResponse(any())).thenReturn(
                ReturnResponse.builder().status("REFUNDED").build());

        businessReturnService.updateStatus(RETURN_ID, request);

        // Item status should be RETURNED
        ArgumentCaptor<OrderItemEntity> itemCaptor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getStatus()).isEqualTo(OrderItemStatus.RETURNED);

        // Event should be published
        ArgumentCaptor<ReturnApprovedEvent> eventCaptor =
                ArgumentCaptor.forClass(ReturnApprovedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().orderItemId()).isEqualTo(ITEM_ID);
        assertThat(eventCaptor.getValue().returnRequestId()).isEqualTo(RETURN_ID);
    }

    // --- updateStatus: REQUESTED → REJECTED ---

    @Test
    void updateStatus_WhenRejectedWithReason_ShouldSetRejectionFields() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.REQUESTED);
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("REJECTED");
        request.setRejectionReason("Product not in original packaging");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));
        when(returnRequestRepository.save(any(ReturnRequestEntity.class))).thenReturn(returnReq);
        when(returnMapper.toResponse(any())).thenReturn(
                ReturnResponse.builder().status("REJECTED").build());

        businessReturnService.updateStatus(RETURN_ID, request);

        assertThat(returnReq.getRejectedAt()).isNotNull();
        assertThat(returnReq.getRejectionReason()).isEqualTo("Product not in original packaging");
        verify(eventPublisher, never()).publishEvent(any(ReturnApprovedEvent.class));
    }

    @Test
    void updateStatus_WhenRejectedWithoutReason_ShouldThrowInvalidOrderState() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.REQUESTED);
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("REJECTED");
        request.setRejectionReason("");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));

        assertThatThrownBy(() -> businessReturnService.updateStatus(RETURN_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Rejection reason");
    }

    // --- invalid transitions ---

    @Test
    void updateStatus_WhenInvalidTransition_ShouldThrowInvalidOrderState() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.REQUESTED);
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("REFUNDED"); // REQUESTED → REFUNDED is not allowed

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));

        assertThatThrownBy(() -> businessReturnService.updateStatus(RETURN_ID, request))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    void updateStatus_WhenReturnNotFound_ShouldThrowResourceNotFound() {
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("PICKUP_SCHEDULED");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessReturnService.updateStatus(RETURN_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- lifecycle timestamps ---

    @Test
    void updateStatus_WhenReceivedStatus_ShouldSetReceivedAt() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.PICKED_UP);
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("RECEIVED");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));
        when(returnRequestRepository.save(any(ReturnRequestEntity.class))).thenReturn(returnReq);
        when(returnMapper.toResponse(any())).thenReturn(
                ReturnResponse.builder().status("RECEIVED").build());

        businessReturnService.updateStatus(RETURN_ID, request);

        assertThat(returnReq.getReceivedAt()).isNotNull();
    }

    @Test
    void updateStatus_WhenInspectedStatus_ShouldSetInspectedAt() {
        ReturnRequestEntity returnReq = buildReturnRequest(RETURN_ID, ReturnStatus.RECEIVED);
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        request.setStatus("INSPECTED");

        when(returnRequestRepository.findById(RETURN_ID)).thenReturn(Optional.of(returnReq));
        when(returnRequestRepository.save(any(ReturnRequestEntity.class))).thenReturn(returnReq);
        when(returnMapper.toResponse(any())).thenReturn(
                ReturnResponse.builder().status("INSPECTED").build());

        businessReturnService.updateStatus(RETURN_ID, request);

        assertThat(returnReq.getInspectedAt()).isNotNull();
    }

    // --- helpers ---

    private ReturnRequestEntity buildReturnRequest(Long id, ReturnStatus status) {
        ReturnRequestEntity entity = ReturnRequestEntity.builder()
                .orderItemId(ITEM_ID)
                .reason("Damaged")
                .status(status)
                .rmaNumber("RMA-12345")
                .build();
        return TestEntityIdUtil.withId(entity, id);
    }

    private OrderItemEntity buildItem(Long id, OrderItemStatus status) {
        OrderItemEntity item = OrderItemEntity.builder()
                .orderId(ORDER_ID)
                .businessId(5L)
                .status(status)
                .build();
        return TestEntityIdUtil.withId(item, id);
    }
}
