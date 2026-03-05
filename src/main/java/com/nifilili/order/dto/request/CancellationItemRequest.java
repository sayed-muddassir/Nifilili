package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancellationItemRequest {

    @NotNull
    private Long orderItemId;

    @NotBlank
    private String reason;

    private String note;
}
