package com.nifilili.business.service.impl;

import com.nifilili.business.domain.DocumentDefinition;
import com.nifilili.business.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.business.dto.response.DocumentDefinitionResponse;
import com.nifilili.business.mapper.DocumentDefinitionMapper;
import com.nifilili.business.repository.DocumentDefinitionRepository;
import com.nifilili.business.service.DocumentDefinitionService;
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
