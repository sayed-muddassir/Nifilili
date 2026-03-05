package com.nifilili.order.controller.owner;

import com.nifilili.order.dto.request.CodPaymentRequest;
import com.nifilili.order.dto.request.UpdateRefundStatusRequest;
import com.nifilili.order.dto.request.VerifyPaymentRequest;
import com.nifilili.order.dto.response.PaymentStatusResponse;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.service.PaymentService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessPaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private RefundService refundService;

    @InjectMocks
    private BusinessPaymentController businessPaymentController;

    @Test
    void allPaymentEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnCorrectStatus() {
        PaymentStatusResponse paymentResp = PaymentStatusResponse.builder().build();
        when(paymentService.getPayment(1L)).thenReturn(paymentResp);

        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest();
        CodPaymentRequest codReq = new CodPaymentRequest();
        UpdateRefundStatusRequest refundReq = new UpdateRefundStatusRequest();
        RefundResponse refundResp = RefundResponse.builder().build();
        when(refundService.updateRefundStatus(2L, refundReq)).thenReturn(refundResp);

        ResponseEntity<PaymentStatusResponse> getResult = businessPaymentController.getPayment(1L);
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(paymentResp, getResult.getBody());

        ResponseEntity<Void> verifyResult = businessPaymentController.verifyPayment(1L, verifyReq);
        assertEquals(HttpStatus.OK, verifyResult.getStatusCode());
        verify(paymentService).verifyPayment(1L, verifyReq);

        ResponseEntity<Void> codResult = businessPaymentController.recordCodPayment(1L, codReq);
        assertEquals(HttpStatus.OK, codResult.getStatusCode());
        verify(paymentService).recordCodPayment(1L, codReq);

        ResponseEntity<RefundResponse> refundResult = businessPaymentController.updateRefundStatus(2L, refundReq);
        assertEquals(HttpStatus.OK, refundResult.getStatusCode());
        assertSame(refundResp, refundResult.getBody());
    }
}
