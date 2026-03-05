package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancellationDecisionRequest {

    @NotNull
    private Boolean approved;

    private String reason;
}
