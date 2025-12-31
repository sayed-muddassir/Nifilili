package com.nifilili.config.service;

import com.nifilili.config.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.config.dto.response.DocumentDefinitionResponse;

import java.util.List;

public interface DocumentDefinitionService {

    DocumentDefinitionResponse create(CreateDocumentDefinitionRequest request);

    List<DocumentDefinitionResponse> getByVertical(Long verticalId);
}
