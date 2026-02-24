package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.business.dto.response.AttributeDefinitionResponse;

import java.util.List;

public interface AttributeDefinitionService {

    AttributeDefinitionResponse create(CreateAttributeDefinitionRequest request);
    AttributeDefinitionResponse update(Long attributeId, CreateAttributeDefinitionRequest request);

    List<AttributeDefinitionResponse> getByVertical(Long verticalId);
}
