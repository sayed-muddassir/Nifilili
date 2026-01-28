package com.nifilili.order.controller.business;

import com.nifilili.order.dto.request.business.RejectOrderItemRequest;
import com.nifilili.order.dto.request.business.UpdateOrderItemStatusRequest;
import com.nifilili.order.dto.response.business.BusinessOrderItemResponse;
import com.nifilili.order.service.business.BusinessOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business/order-items")
@RequiredArgsConstructor
public class BusinessOrderItemController {

    private final BusinessOrderItemService businessOrderItemService;

    @GetMapping
    public Page<BusinessOrderItemResponse> listItems(
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        return businessOrderItemService.listItems(status, pageable);
    }

    @PutMapping("/{orderItemId}/status")
    public void updateStatus(
            @PathVariable Long orderItemId,
            @RequestBody UpdateOrderItemStatusRequest request
    ) {
        businessOrderItemService.updateStatus(orderItemId, request);
    }

    @PutMapping("/{orderItemId}/reject")
    public void rejectItem(
            @PathVariable Long orderItemId,
            @RequestBody RejectOrderItemRequest request
    ) {
        businessOrderItemService.reject(orderItemId, request);
    }
}

