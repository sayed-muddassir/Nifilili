package com.nifilili.order.dto.response.orderplacement;

import com.nifilili.order.dto.response.payment.PaymentSummaryResponse;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailsResponse {
    private String orderNumber;
    private String status;
    private List<OrderItemDetailsResponse> items;
    private PaymentSummaryResponse payment;
}

