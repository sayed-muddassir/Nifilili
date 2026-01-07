package com.nifilili.business.validation;

import java.util.List;

public class SectionValidationException extends RuntimeException {

    private final List<String> errors;

    public SectionValidationException(List<String> errors) {
        super("SectionDefinition validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
