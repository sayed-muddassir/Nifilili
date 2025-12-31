package com.nifilili.business.validation;

import com.nifilili.business.validation.validators.*;

import java.util.EnumMap;
import java.util.Map;

public class FieldValidatorRegistry {

    private static final Map<FieldType, FieldValidator> VALIDATORS =
            new EnumMap<>(FieldType.class);

    static {
        VALIDATORS.put(FieldType.TEXT, new TextFieldValidator());
        VALIDATORS.put(FieldType.NUMBER, new NumberFieldValidator());
        VALIDATORS.put(FieldType.DROPDOWN, new DropdownFieldValidator());
        VALIDATORS.put(FieldType.CHECKBOX, new CheckboxFieldValidator());
        VALIDATORS.put(FieldType.MEDIA_URL, new MediaUrlValidator());
    }

    public static FieldValidator get(FieldType type) {
        return VALIDATORS.get(type);
    }
}
