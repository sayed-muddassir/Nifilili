package com.nifilili.business.validation;

import java.util.List;

public record SectionFieldMetadata(
        String name,
        FieldType type,
        boolean required,
        boolean allowMultiple,
        List<String> options
) {}
