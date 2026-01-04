package com.nifilili.config.mapper;

import com.nifilili.config.domain.AttributeDefinition;
import com.nifilili.config.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.config.dto.response.AttributeDefinitionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    AttributeDefinition toEntity(CreateAttributeDefinitionRequest request);

    AttributeDefinitionResponse toResponse(AttributeDefinition entity);
}