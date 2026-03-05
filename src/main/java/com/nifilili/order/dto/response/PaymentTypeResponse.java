package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentTypeResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
}
