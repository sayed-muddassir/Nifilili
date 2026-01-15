package com.nifilili.offering.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        Long parentCategoryId
) {}
