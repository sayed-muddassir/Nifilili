package com.nifilili.offering.dto.response;

import com.nifilili.core.enums.util.AttributeType;

import java.util.List;

public record AttributeResponse(
        Long id,
        Long offeringCategoryId,
        String name,
        AttributeType attributeType,
        List<String> options
) {}

