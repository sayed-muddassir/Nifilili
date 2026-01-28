package com.nifilili.order.controller.refund;

import com.nifilili.order.dto.request.refund.CreateRefundRequest;
import com.nifilili.order.dto.request.refund.UpdateRefundStatusRequest;
import com.nifilili.order.dto.response.refund.RefundResponse;
import com.nifilili.order.service.refund.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/orders/{orderId}/refunds")
    public RefundResponse createRefund(
            @PathVariable Long orderId,
            @RequestBody CreateRefundRequest request
    ) {
        return refundService.createRefund(orderId, request);
    }

    @PutMapping("/refunds/{refundId}/status")
    public void updateRefundStatus(
            @PathVariable Long refundId,
            @RequestBody UpdateRefundStatusRequest request
    ) {
        refundService.updateRefundStatus(refundId, request);
    }
}
