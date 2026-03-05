package com.nifilili.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBusinessConfigRequest {

    @NotBlank
    private String configKey;

    @NotBlank
    private String configValue;
}
