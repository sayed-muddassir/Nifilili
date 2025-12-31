package com.nifilili.business.dto.request;

import lombok.Data;

@Data
public class SaveBusinessAttributeRequest {

    private Long attributeId;

    /**
     * Can be:
     * - Boolean
     * - Number
     * - String
     * - List<String>
     */
    private Object attributeValue;
}