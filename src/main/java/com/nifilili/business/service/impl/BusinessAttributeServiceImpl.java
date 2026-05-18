package com.nifilili.business.service.impl;

import com.nifilili.business.domain.AttributeDefinition;
import com.nifilili.business.domain.Business;
import com.nifilili.business.domain.BusinessAttribute;
import com.nifilili.business.dto.request.SaveBusinessAttributeRequest;
import com.nifilili.business.repository.AttributeDefinitionRepository;
import com.nifilili.business.repository.BusinessAttributeRepository;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessAttributeService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessAttributeServiceImpl implements BusinessAttributeService {


    private final AttributeDefinitionRepository definitionRepository;
    private final BusinessAttributeRepository repository;
    private final BusinessRepository businessRepository;

    @Override
    public void saveAttributeData(Long businessId, SaveBusinessAttributeRequest request) {
        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);

        AttributeDefinition def = definitionRepository
                .findById(request.getAttributeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attribute not found"));

        validateAttributeData(def, request.getAttributeValue());

        repository.deleteByBusinessIdAndAttributeId(
                businessId,
                request.getAttributeId()
        );

        BusinessAttribute attribute = new BusinessAttribute(
                businessId,
                request.getAttributeId(),
                Map.of("value", request.getAttributeValue()),
                Instant.now(),
                Instant.now()
        );

        repository.save(attribute);
    }

    private Business loadOwnedBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Long authenticatedUserId = SecurityUtil.getCurrentUserId();
        if (business.getOwnerUserId() == null || !business.getOwnerUserId().equals(authenticatedUserId)) {
            throw new InvalidBusinessStateException("Unauthorized access");
        }

        return business;
    }

    private void ensureEditable(Business business) {
        if (business.getStatus() == BusinessStatus.DRAFT) {
            throw new InvalidBusinessStateException("Business cannot be edited in current state");
        }
    }

    private void validateAttributeData(AttributeDefinition def, Object value) {

        if (value == null && def.isRequired()) {
            throw new IllegalArgumentException("Attribute is required");
        }

        switch (def.getType().toUpperCase()) {

            case "BOOLEAN" -> {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("Expected boolean");
                }
            }

            case "NUMBER" -> {
                if (!(value instanceof Number)) {
                    throw new IllegalArgumentException("Expected number");
                }
            }

            case "TEXT" -> {
                if (!(value instanceof String s) || s.isBlank()) {
                    throw new IllegalArgumentException("Expected non-empty text");
                }
            }

            case "DROPDOWN" -> {
                if (!(value instanceof String)
                        || !def.getOptions().contains(value)) {
                    throw new IllegalArgumentException("Invalid dropdown value");
                }
            }

            case "CHECKBOX" -> {
                if (!(value instanceof Iterable<?> list)) {
                    throw new IllegalArgumentException("Expected list");
                }
                for (Object v : list) {
                    if (!def.getOptions().contains(v)) {
                        throw new IllegalArgumentException("Invalid checkbox option");
                    }
                }
            }

            default -> throw new IllegalArgumentException("Unsupported attribute type");
        }
    }
}
