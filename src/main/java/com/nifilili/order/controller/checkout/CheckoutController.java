package com.nifilili.order.controller.checkout;

import com.nifilili.order.dto.request.checkout.CheckoutPreviewRequest;
import com.nifilili.order.dto.response.checkout.CheckoutPreviewResponse;
import com.nifilili.order.service.checkout.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preview")
    public CheckoutPreviewResponse preview(@RequestBody CheckoutPreviewRequest request) {
        return checkoutService.preview(request);
    }
}

