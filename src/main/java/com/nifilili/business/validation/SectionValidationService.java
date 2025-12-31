package com.nifilili.business.validation;

import com.nifilili.config.domain.SectionField;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SectionValidationService {

    public void validate(
            List<SectionField> fields,
            Map<String, Object> values
    ) {

        List<String> errors = new ArrayList<>();

        Map<String, SectionField> fieldMap = new HashMap<>();
        for (SectionField field : fields) {
            fieldMap.put(field.getName(), field);
        }

        // 1️⃣ Unknown fields
        for (String inputKey : values.keySet()) {
            if (!fieldMap.containsKey(inputKey)) {
                errors.add("Unknown field: " + inputKey);
            }
        }

        // 2️⃣ Required fields
        for (SectionField field : fields) {
            if (field.isRequired() && !values.containsKey(field.getName())) {
                errors.add("Missing required field: " + field.getName());
            }
        }

        // 3️⃣ Type validation
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            try {
                SectionField field = fieldMap.get(entry.getKey());

                SectionFieldMetadata meta =
                        new SectionFieldMetadata(
                                field.getName(),
                                FieldType.valueOf(field.getType().toUpperCase()),
                                field.isRequired(),
                                field.isAllowMultiple(),
                                field.getOptions()
                        );

                FieldValidator validator =
                        FieldValidatorRegistry.get(meta.type());

                validator.validate(
                        entry.getKey(),
                        entry.getValue(),
                        meta
                );

            } catch (Exception ex) {
                errors.add(ex.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new SectionValidationException(errors);
        }
    }
}
