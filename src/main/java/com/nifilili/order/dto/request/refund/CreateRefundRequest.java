package com.nifilili.order.dto.request.refund;

import lombok.Data;

@Data
public class CreateRefundRequest {
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String branch;
}

