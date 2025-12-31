package com.nifilili.business.validation.validators;

import com.nifilili.business.validation.FieldValidator;
import com.nifilili.business.validation.SectionFieldMetadata;

import java.util.List;

public class CheckboxFieldValidator implements FieldValidator {

    @Override
    public void validate(String fieldName, Object value, SectionFieldMetadata meta) {

        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a list");
        }

        for (Object v : list) {
            if (!meta.options().contains(v)) {
                throw new IllegalArgumentException(
                        fieldName + " has invalid option: " + v);
            }
        }
    }
}
