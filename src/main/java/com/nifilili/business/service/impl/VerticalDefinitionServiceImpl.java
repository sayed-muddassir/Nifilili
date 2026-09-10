package com.nifilili.business.service.impl;

import com.nifilili.business.domain.VerticalDefinition;
import com.nifilili.business.dto.request.CreateVerticalRequest;
import com.nifilili.business.dto.response.VerticalResponse;
import com.nifilili.business.mapper.VerticalMapper;
import com.nifilili.business.repository.VerticalRepository;
import com.nifilili.business.service.VerticalDefinitionService;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerticalDefinitionServiceImpl implements VerticalDefinitionService {

    private final VerticalRepository repository;
    private final VerticalMapper mapper;

    @Override
    public List<VerticalResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public VerticalResponse create(CreateVerticalRequest request) {
        VerticalDefinition saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public VerticalResponse update(Long verticalId, CreateVerticalRequest request) {
        VerticalDefinition existing = repository.findById(verticalId)
                .orElseThrow(() -> new ResourceNotFoundException("Vertical not found"));

        existing.setName(request.getName());
        existing.setSlug(request.getSlug());
        existing.setDescription(request.getDescription());
        existing.setIconUrl(request.getIconUrl());
        existing.setActive(request.isActive());

        return mapper.toResponse(repository.save(existing));
    }

    @Override
    public List<VerticalResponse> getAllActive() {
        return repository.findAll()
                .stream()
                .filter(VerticalDefinition::isActive)
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long verticalId) {
        VerticalDefinition existing = repository.findById(verticalId)
                .orElseThrow(() -> new ResourceNotFoundException("Vertical not found"));
        repository.delete(existing);
    }
}
