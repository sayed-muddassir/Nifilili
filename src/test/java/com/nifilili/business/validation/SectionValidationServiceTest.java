package com.nifilili.business.validation;

import com.nifilili.business.domain.SectionField;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SectionValidationServiceTest {

    private final SectionValidationService validationService = new SectionValidationService();

    @Test
    void validate_WhenInputMatchesFieldDefinitions_ShouldPass() {
        List<SectionField> fieldDefinitions = List.of(
                new SectionField(1L, "title", "Title", "TEXT", List.of(), true, false),
                new SectionField(1L, "rating", "Rating", "NUMBER", List.of(), false, false),
                new SectionField(1L, "category", "Category", "DROPDOWN", List.of("A", "B"), true, false)
        );

        Map<String, Object> values = Map.of(
                "title", "Sample",
                "rating", 4,
                "category", "A"
        );

        assertDoesNotThrow(() -> validationService.validate(fieldDefinitions, values));
    }

    @Test
    void validate_WhenUnknownAndMissingFieldsExist_ShouldCollectAllErrors() {
        List<SectionField> fieldDefinitions = List.of(
                new SectionField(1L, "title", "Title", "TEXT", List.of(), true, false)
        );

        SectionValidationException ex = assertThrows(
                SectionValidationException.class,
                () -> validationService.validate(fieldDefinitions, Map.of("unknown", "x"))
        );

        assertTrue(ex.getErrors().stream().anyMatch(message -> message.contains("Unknown field: unknown")));
        assertTrue(ex.getErrors().stream().anyMatch(message -> message.contains("Missing required field: title")));
    }

    @Test
    void validate_WhenInvalidFieldTypeProvided_ShouldFailValidation() {
        List<SectionField> fieldDefinitions = List.of(
                new SectionField(1L, "rating", "Rating", "NUMBER", List.of(), true, false)
        );

        SectionValidationException ex = assertThrows(
                SectionValidationException.class,
                () -> validationService.validate(fieldDefinitions, Map.of("rating", "not-a-number"))
        );

        assertTrue(ex.getErrors().stream().anyMatch(message -> message.contains("rating must be a number")));
    }
}
