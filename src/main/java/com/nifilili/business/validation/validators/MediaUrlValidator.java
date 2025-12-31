package com.nifilili.business.validation.validators;

import com.nifilili.business.validation.FieldValidator;
import com.nifilili.business.validation.SectionFieldMetadata;

public class MediaUrlValidator implements FieldValidator {

    @Override
    public void validate(String fieldName, Object value, SectionFieldMetadata meta) {
        if (!(value instanceof String url) || !url.startsWith("http")) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid URL");
        }
    }
}
