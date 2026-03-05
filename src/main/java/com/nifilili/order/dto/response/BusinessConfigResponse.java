package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
}
