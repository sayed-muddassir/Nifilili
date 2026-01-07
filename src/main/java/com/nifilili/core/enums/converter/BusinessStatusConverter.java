package com.nifilili.core.enums.converter;

import com.nifilili.core.enums.BusinessStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BusinessStatusConverter
        implements AttributeConverter<BusinessStatus, String> {

    @Override
    public String convertToDatabaseColumn(BusinessStatus status) {
        return status != null ? status.name() : null;
    }

    @Override
    public BusinessStatus convertToEntityAttribute(String dbValue) {
        return dbValue != null ? BusinessStatus.valueOf(dbValue) : null;
    }
}
