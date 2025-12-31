package com.nifilili.business.validation;

public interface FieldValidator {

    void validate(String fieldName, Object value, SectionFieldMetadata meta);
}
