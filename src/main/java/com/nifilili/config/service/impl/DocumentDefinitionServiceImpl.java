package com.nifilili.config.service.impl;

import com.nifilili.config.domain.DocumentDefinition;
import com.nifilili.config.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.config.dto.response.DocumentDefinitionResponse;
import com.nifilili.config.mapper.DocumentDefinitionMapper;
import com.nifilili.config.repository.DocumentDefinitionRepository;
import com.nifilili.config.service.DocumentDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentDefinitionServiceImpl implements DocumentDefinitionService {

    private final DocumentDefinitionRepository repository;
    private final DocumentDefinitionMapper mapper;

    @Override
    public DocumentDefinitionResponse create(CreateDocumentDefinitionRequest request) {
        DocumentDefinition saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public List<DocumentDefinitionResponse> getByVertical(Long verticalId) {
        return repository.findByVerticalId(verticalId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
