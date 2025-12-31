package com.nifilili.business.dto.response;

import lombok.Data;

@Data
public class BusinessAttributeResponse {

    private Long attributeId;
    private String name;
    private Object attributeValue;
}