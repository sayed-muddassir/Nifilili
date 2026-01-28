package com.nifilili.order.controller.payment;

import com.nifilili.order.dto.request.payment.CodPaymentRequest;
import com.nifilili.order.dto.request.payment.VerifyPaymentRequest;
import com.nifilili.order.dto.response.payment.PaymentStatusResponse;
import com.nifilili.order.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public PaymentStatusResponse getPayment(@PathVariable Long orderId) {
        return paymentService.getPayment(orderId);
    }

    @PutMapping("/verify")
    public void verifyPayment(
            @PathVariable Long orderId,
            @RequestBody VerifyPaymentRequest request
    ) {
        paymentService.verifyPayment(orderId, request);
    }

    @PostMapping("/cod")
    public void recordCodPayment(
            @PathVariable Long orderId,
            @RequestBody CodPaymentRequest request
    ) {
        paymentService.recordCodPayment(orderId, request);
    }
}
