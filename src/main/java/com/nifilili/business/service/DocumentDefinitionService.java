package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.business.dto.response.DocumentDefinitionResponse;

import java.util.List;

public interface DocumentDefinitionService {

    DocumentDefinitionResponse create(CreateDocumentDefinitionRequest request);
    DocumentDefinitionResponse update(Long documentDefinitionId, CreateDocumentDefinitionRequest request);

    List<DocumentDefinitionResponse> getByVertical(Long verticalId);

    void delete(Long documentDefinitionId);
}
