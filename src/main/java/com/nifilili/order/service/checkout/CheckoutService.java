package com.nifilili.order.service.checkout;


import com.nifilili.order.dto.request.checkout.CheckoutPreviewRequest;
import com.nifilili.order.dto.response.checkout.CheckoutPreviewResponse;

public interface CheckoutService {
    CheckoutPreviewResponse preview(CheckoutPreviewRequest request);
}

