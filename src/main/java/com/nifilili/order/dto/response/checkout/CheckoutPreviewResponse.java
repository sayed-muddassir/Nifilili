package com.nifilili.order.dto.response.checkout;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutPreviewResponse {
    private List<CheckoutItemPreviewResponse> items;
    private CheckoutSummaryResponse summary;
}

