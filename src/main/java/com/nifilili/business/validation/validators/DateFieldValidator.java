package com.nifilili.business.validation.validators;

import com.nifilili.business.validation.FieldValidator;
import com.nifilili.business.validation.SectionFieldMetadata;

public class DateFieldValidator implements FieldValidator {

    @Override
    public void validate(String fieldName, Object value, SectionFieldMetadata meta) {
        if (!(value instanceof String dateStr)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a string");
        }

        // Date must be in the format of DD-MM-YYYY
        if (!dateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
            throw new IllegalArgumentException(
                    fieldName
                            + " has invalid date format: "
                            + dateStr
                            + " accepted date format DD-MM-YYYY");
        }
    }
}
