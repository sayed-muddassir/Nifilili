package com.nifilili.business.service.impl;

import com.nifilili.business.service.VerticalDefinitionService;
import com.nifilili.business.domain.VerticalDefinition;
import com.nifilili.business.dto.request.CreateVerticalRequest;
import com.nifilili.business.dto.response.VerticalResponse;
import com.nifilili.business.mapper.VerticalMapper;
import com.nifilili.business.repository.VerticalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerticalDefinitionServiceImpl implements VerticalDefinitionService {

    private final VerticalRepository repository;
    private final VerticalMapper mapper;

    @Override
    public VerticalResponse create(CreateVerticalRequest request) {
        VerticalDefinition saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public List<VerticalResponse> getAllActive() {
        return repository.findAll()
                .stream()
                .filter(VerticalDefinition::isActive)
                .map(mapper::toResponse)
                .toList();
    }
}
