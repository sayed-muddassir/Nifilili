package com.nifilili.config.service;

import com.nifilili.config.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.config.dto.response.AttributeDefinitionResponse;

import java.util.List;

public interface AttributeDefinitionService {

    AttributeDefinitionResponse create(CreateAttributeDefinitionRequest request);

    List<AttributeDefinitionResponse> getByVertical(Long verticalId);
}