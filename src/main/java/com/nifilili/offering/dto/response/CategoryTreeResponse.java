package com.nifilili.offering.dto.response;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        List<CategoryTreeResponse> children
) {}

