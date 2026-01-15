package com.nifilili.offering.dto.request;

public record CreateCategoryRequest(
        String name,
        Long parentCategoryId
) {}
