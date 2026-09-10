package com.nifilili.business.validation.validators;

import com.nifilili.business.validation.FieldValidator;
import com.nifilili.business.validation.SectionFieldMetadata;

public class TimeFieldValidator implements FieldValidator {

    @Override
    public void validate(String fieldName, Object value, SectionFieldMetadata meta) {
        if (!(value instanceof String timeValue)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a string");
        }

        // Time must be in the format of HH:MM (24-hour format)
        if (!timeValue.matches("([01]\\d|2[0-3]):([0-5]\\d)")) {
            throw new IllegalArgumentException(
                    fieldName
                            + " has invalid time format: "
                            + timeValue
                            + " Accepted time format HH:MM");
        }
    }
}
