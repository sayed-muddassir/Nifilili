package com.nifilili.business.service.impl;

import com.nifilili.business.domain.AttributeDefinition;
import com.nifilili.business.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.business.dto.response.AttributeDefinitionResponse;
import com.nifilili.business.mapper.AttributeDefinitionMapper;
import com.nifilili.business.repository.AttributeDefinitionRepository;
import com.nifilili.business.service.AttributeDefinitionService;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeDefinitionServiceImpl
        implements AttributeDefinitionService {

    private final AttributeDefinitionRepository repository;
    private final AttributeDefinitionMapper mapper;

    @Override
    public AttributeDefinitionResponse create(CreateAttributeDefinitionRequest request) {
        AttributeDefinition saved =
                repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public AttributeDefinitionResponse update(Long attributeId, CreateAttributeDefinitionRequest request) {
        AttributeDefinition existing = repository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found"));

        existing.setVerticalId(request.getVerticalId());
        existing.setName(request.getName());
        existing.setLabel(request.getLabel());
        existing.setType(request.getType());
        existing.setOptions(request.getOptions());
        existing.setRequired(request.isRequired());
        existing.setAllowMultiple(request.isAllowMultiple());
        existing.setPrompt(request.getPrompt());
        existing.setUpdatedAt(java.time.Instant.now());

        return mapper.toResponse(repository.save(existing));
    }

    @Override
    public List<AttributeDefinitionResponse> getByVertical(Long verticalId) {
        return repository.findByVerticalId(verticalId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
