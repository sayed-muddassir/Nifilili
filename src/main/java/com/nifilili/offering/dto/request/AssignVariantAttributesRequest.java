package com.nifilili.offering.dto.request;

import java.util.List;

public record AssignVariantAttributesRequest(
        List<VariantAttributeItem> attributes
) {}

