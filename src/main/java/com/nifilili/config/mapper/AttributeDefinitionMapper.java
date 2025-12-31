package com.nifilili.config.mapper;

import com.nifilili.config.domain.AttributeDefinition;
import com.nifilili.config.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.config.dto.response.AttributeDefinitionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {

    AttributeDefinition toEntity(CreateAttributeDefinitionRequest request);

    AttributeDefinitionResponse toResponse(AttributeDefinition entity);
}