package com.nifilili.order.controller.cancellation;

import com.nifilili.order.dto.request.cancellation.CancellationDecisionRequest;
import com.nifilili.order.dto.request.cancellation.CancellationRequest;
import com.nifilili.order.service.cancellation.CancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/cancellations")
@RequiredArgsConstructor
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping
    public void requestCancellation(
            @PathVariable Long orderId,
            @RequestBody CancellationRequest request
    ) {
        cancellationService.requestCancellation(orderId, request);
    }

    @PutMapping("/items/{orderItemId}")
    public void decideCancellation(
            @PathVariable Long orderItemId,
            @RequestBody CancellationDecisionRequest request
    ) {
        cancellationService.decide(orderItemId, request);
    }
}

