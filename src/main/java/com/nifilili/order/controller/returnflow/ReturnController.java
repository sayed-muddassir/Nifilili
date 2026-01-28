package com.nifilili.order.controller.returnflow;

import com.nifilili.order.dto.request.returnflow.CreateReturnRequest;
import com.nifilili.order.dto.response.returnflow.ReturnResponse;
import com.nifilili.order.service.returnflow.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/{orderItemId}/returns")
    public ReturnResponse requestReturn(
            @PathVariable Long orderItemId,
            @RequestBody CreateReturnRequest request
    ) {
        return returnService.createReturn(orderItemId, request);
    }
}

