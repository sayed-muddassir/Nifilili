package com.nifilili.order.controller.publicapi;

import com.nifilili.order.dto.request.CreateRefundRequest;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.service.RefundService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundControllerTest {

    @Mock
    private RefundService refundService;

    @InjectMocks
    private RefundController refundController;

    @Test
    void createRefund_WhenServiceReturnsResponse_ShouldDelegateAndReturnCreated() {
        CreateRefundRequest request = new CreateRefundRequest();
        RefundResponse response = RefundResponse.builder().build();
        when(refundService.createRefund(1L, request)).thenReturn(response);

        ResponseEntity<RefundResponse> result = refundController.createRefund(1L, request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
    }
}
