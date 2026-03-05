package com.nifilili.order.controller.owner;

import com.nifilili.order.dto.request.CancellationDecisionRequest;
import com.nifilili.order.dto.request.RejectOrderItemRequest;
import com.nifilili.order.dto.request.UpdateOrderItemStatusRequest;
import com.nifilili.order.dto.response.CancellationResponse;
import com.nifilili.order.service.CancellationService;
import com.nifilili.order.service.OrderItemLifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessOrderItemControllerTest {

    @Mock
    private OrderItemLifecycleService orderItemLifecycleService;

    @Mock
    private CancellationService cancellationService;

    @InjectMocks
    private BusinessOrderItemController businessOrderItemController;

    @Test
    void allBusinessOrderItemEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnOk() {
        UpdateOrderItemStatusRequest statusReq = new UpdateOrderItemStatusRequest();
        RejectOrderItemRequest rejectReq = new RejectOrderItemRequest();
        CancellationDecisionRequest decisionReq = new CancellationDecisionRequest();
        CancellationResponse decisionResp = CancellationResponse.builder().build();
        when(cancellationService.decide(1L, decisionReq)).thenReturn(decisionResp);

        ResponseEntity<Void> statusResult = businessOrderItemController.updateStatus(1L, statusReq);
        assertEquals(HttpStatus.OK, statusResult.getStatusCode());
        verify(orderItemLifecycleService).updateStatus(1L, statusReq);

        ResponseEntity<Void> rejectResult = businessOrderItemController.reject(1L, rejectReq);
        assertEquals(HttpStatus.OK, rejectResult.getStatusCode());
        verify(orderItemLifecycleService).reject(1L, rejectReq);

        ResponseEntity<CancellationResponse> decisionResult =
                businessOrderItemController.decideCancellation(1L, decisionReq);
        assertEquals(HttpStatus.OK, decisionResult.getStatusCode());
        assertSame(decisionResp, decisionResult.getBody());
    }
}
