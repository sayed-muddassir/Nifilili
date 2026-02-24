package com.nifilili.business.validation;

import com.nifilili.business.validation.validators.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldValidatorsTest {

    @Test
    void textFieldValidator_WhenValueIsString_ShouldPass() {
        TextFieldValidator validator = new TextFieldValidator();
        assertDoesNotThrow(() -> validator.validate("title", "hello", new SectionFieldMetadata("title", FieldType.TEXT, true, false, List.of())));
    }

    @Test
    void textFieldValidator_WhenValueIsNotString_ShouldFail() {
        TextFieldValidator validator = new TextFieldValidator();
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("title", 123, new SectionFieldMetadata("title", FieldType.TEXT, true, false, List.of())));
    }

    @Test
    void numberFieldValidator_WhenValueIsNumber_ShouldPass() {
        NumberFieldValidator validator = new NumberFieldValidator();
        assertDoesNotThrow(() -> validator.validate("score", 99, new SectionFieldMetadata("score", FieldType.NUMBER, true, false, List.of())));
    }

    @Test
    void dropdownFieldValidator_WhenValueExistsInOptions_ShouldPass() {
        DropdownFieldValidator validator = new DropdownFieldValidator();
        assertDoesNotThrow(() -> validator.validate("type", "A", new SectionFieldMetadata("type", FieldType.DROPDOWN, true, false, List.of("A", "B"))));
    }

    @Test
    void dropdownFieldValidator_WhenValueIsNotInOptions_ShouldFail() {
        DropdownFieldValidator validator = new DropdownFieldValidator();
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("type", "C", new SectionFieldMetadata("type", FieldType.DROPDOWN, true, false, List.of("A", "B"))));
    }

    @Test
    void checkboxFieldValidator_WhenAllValuesExistInOptions_ShouldPass() {
        CheckboxFieldValidator validator = new CheckboxFieldValidator();
        assertDoesNotThrow(() -> validator.validate("features", List.of("A", "B"), new SectionFieldMetadata("features", FieldType.CHECKBOX, false, true, List.of("A", "B", "C"))));
    }

    @Test
    void checkboxFieldValidator_WhenOptionIsInvalid_ShouldFail() {
        CheckboxFieldValidator validator = new CheckboxFieldValidator();
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("features", List.of("A", "Z"), new SectionFieldMetadata("features", FieldType.CHECKBOX, false, true, List.of("A", "B", "C"))));
    }

    @Test
    void mediaUrlValidator_WhenValueIsValidUrl_ShouldPass() {
        MediaUrlValidator validator = new MediaUrlValidator();
        assertDoesNotThrow(() -> validator.validate("image", "http://cdn/image.png", new SectionFieldMetadata("image", FieldType.MEDIA_URL, false, false, List.of())));
    }

    @Test
    void mediaUrlValidator_WhenValueIsInvalidUrl_ShouldFail() {
        MediaUrlValidator validator = new MediaUrlValidator();
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("image", "invalid-url", new SectionFieldMetadata("image", FieldType.MEDIA_URL, false, false, List.of())));
    }
}
