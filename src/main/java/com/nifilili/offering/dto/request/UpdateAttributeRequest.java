package com.nifilili.offering.dto.request;

import com.nifilili.core.enums.util.AttributeType;

import java.util.List;

public record UpdateAttributeRequest(
        String name,
        AttributeType attributeType,
        List<String> options
) {}
