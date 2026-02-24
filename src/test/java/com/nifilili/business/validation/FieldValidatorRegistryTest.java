package com.nifilili.business.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FieldValidatorRegistryTest {

    @Test
    void get_WhenFieldTypeIsKnown_ShouldReturnValidatorInstance() {
        assertNotNull(FieldValidatorRegistry.get(FieldType.TEXT));
        assertNotNull(FieldValidatorRegistry.get(FieldType.NUMBER));
        assertNotNull(FieldValidatorRegistry.get(FieldType.DROPDOWN));
        assertNotNull(FieldValidatorRegistry.get(FieldType.CHECKBOX));
        assertNotNull(FieldValidatorRegistry.get(FieldType.MEDIA_URL));
    }
}
