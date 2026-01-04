package com.nifilili.business.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessAttributeResponse {

    private Long attributeId;
    private String name;
    private Object attributeValue;
}