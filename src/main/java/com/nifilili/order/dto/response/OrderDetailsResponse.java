package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailsResponse {
    private String orderNumber;
    private String status;
    private String receiverName;
    private String contactNumber;
    private String email;
    private String address;
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private List<OrderItemDetailsResponse> items;
    private PaymentSummaryResponse payment;
    private String customerNotes;
    private LocalDateTime createdAt;
}
