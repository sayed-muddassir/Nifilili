package com.nifilili.order.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CodPaymentRequest;
import com.nifilili.order.dto.request.UpdateRefundStatusRequest;
import com.nifilili.order.dto.request.VerifyPaymentRequest;
import com.nifilili.order.dto.response.PaymentStatusResponse;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.service.PaymentService;
import com.nifilili.order.service.RefundService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/business/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_9, description = "Business payment and refund management")
@Hidden
public class BusinessPaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @GetMapping("/{orderId}/payment")
    @Operation(summary = "Step 9.1: Get Payment Status", description = "Returns payment details for an order")
    @ApiResponse(responseCode = "200", description = "Payment returned")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentStatusResponse> getPayment(@PathVariable Long orderId) {
        log.info("GET /api/v1/business/orders/{}/payment", orderId);
        return ResponseEntity.ok(paymentService.getPayment(orderId));
    }

    @PutMapping("/{orderId}/payment/verify")
    @Operation(summary = "Step 9.2: Verify Payment", description = "Verifies a manual bank transfer payment")
    @ApiResponse(responseCode = "200", description = "Payment verified")
    @ApiResponse(responseCode = "400", description = "Payment not in verifiable state")
    public ResponseEntity<Void> verifyPayment(@PathVariable Long orderId,
                                               @Valid @RequestBody VerifyPaymentRequest request) {
        log.info("PUT /api/v1/business/orders/{}/payment/verify", orderId);
        paymentService.verifyPayment(orderId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/payment/cod")
    @Operation(summary = "Step 9.3: Record COD Payment", description = "Records a cash-on-delivery payment upon delivery")
    @ApiResponse(responseCode = "200", description = "COD payment recorded")
    @ApiResponse(responseCode = "400", description = "Payment not in pending state")
    public ResponseEntity<Void> recordCodPayment(@PathVariable Long orderId,
                                                   @Valid @RequestBody CodPaymentRequest request) {
        log.info("PUT /api/v1/business/orders/{}/payment/cod", orderId);
        paymentService.recordCodPayment(orderId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/refunds/{refundId}/status")
    @Operation(summary = "Step 9.4: Update Refund Status", description = "Updates refund status through approval and processing")
    @ApiResponse(responseCode = "200", description = "Refund status updated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Refund not found")
    public ResponseEntity<RefundResponse> updateRefundStatus(@PathVariable Long refundId,
                                                              @Valid @RequestBody UpdateRefundStatusRequest request) {
        log.info("PUT /api/v1/business/orders/refunds/{}/status", refundId);
        return ResponseEntity.ok(refundService.updateRefundStatus(refundId, request));
    }
}
