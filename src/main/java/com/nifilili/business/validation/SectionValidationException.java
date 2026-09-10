package com.nifilili.business.validation;

import lombok.Getter;

import java.util.List;

@Getter
public class SectionValidationException extends RuntimeException {

    private final List<String> errors;

    public SectionValidationException(List<String> errors) {
        super("SectionDefinition validation failed");
        this.errors = errors;
    }

}
