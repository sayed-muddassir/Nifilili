package com.nifilili.offering.dto.response;

public record VariantAttributeDetailResponse(
        Long id,
        Long offeringAttributeId,
        String attributeName,
        String attributeValue
) {}
