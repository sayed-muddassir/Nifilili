package com.nifilili.business.service.impl;

import com.nifilili.business.domain.AttributeDefinition;
import com.nifilili.business.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.business.dto.response.AttributeDefinitionResponse;
import com.nifilili.business.mapper.AttributeDefinitionMapper;
import com.nifilili.business.repository.AttributeDefinitionRepository;
import com.nifilili.business.service.AttributeDefinitionService;
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
