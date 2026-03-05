package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRefundRequest {

    @NotBlank
    private String bankName;

    @NotBlank
    private String accountHolder;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String branch;
}
