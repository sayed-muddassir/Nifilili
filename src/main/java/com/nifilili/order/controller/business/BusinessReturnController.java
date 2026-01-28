package com.nifilili.order.controller.business;

import com.nifilili.order.dto.request.returnflow.UpdateReturnStatusRequest;
import com.nifilili.order.service.business.BusinessReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business/returns")
@RequiredArgsConstructor
public class BusinessReturnController {

    private final BusinessReturnService businessReturnService;

    @PutMapping("/{returnRequestId}/status")
    public void updateReturnStatus(
            @PathVariable Long returnRequestId,
            @RequestBody UpdateReturnStatusRequest request
    ) {
        businessReturnService.updateStatus(returnRequestId, request);
    }
}
