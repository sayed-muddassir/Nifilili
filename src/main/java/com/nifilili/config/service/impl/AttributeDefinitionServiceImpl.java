package com.nifilili.config.service.impl;

import com.nifilili.config.domain.AttributeDefinition;
import com.nifilili.config.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.config.dto.response.AttributeDefinitionResponse;
import com.nifilili.config.mapper.AttributeDefinitionMapper;
import com.nifilili.config.repository.AttributeDefinitionRepository;
import com.nifilili.config.service.AttributeDefinitionService;
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
    public List<AttributeDefinitionResponse> getByVertical(Long verticalId) {
        return repository.findByVerticalId(verticalId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
