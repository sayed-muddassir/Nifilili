package com.nifilili.business.validation.validators;

import com.nifilili.business.validation.FieldValidator;
import com.nifilili.business.validation.SectionFieldMetadata;

public class TextFieldValidator implements FieldValidator {

    @Override
    public void validate(String fieldName, Object value, SectionFieldMetadata meta) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a string");
        }
    }
}
