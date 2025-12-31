package com.nifilili.business.validation.validators;

import com.nifilili.business.validation.FieldValidator;
import com.nifilili.business.validation.SectionFieldMetadata;

public class DropdownFieldValidator implements FieldValidator {

    @Override
    public void validate(String fieldName, Object value, SectionFieldMetadata meta) {

        if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a string");
        }

        if (!meta.options().contains(value)) {
            throw new IllegalArgumentException(
                    fieldName + " has invalid value: " + value);
        }
    }
}
