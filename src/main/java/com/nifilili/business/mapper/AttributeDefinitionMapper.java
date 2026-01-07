package com.nifilili.business.mapper;

import com.nifilili.business.domain.AttributeDefinition;
import com.nifilili.business.dto.request.CreateAttributeDefinitionRequest;
import com.nifilili.business.dto.response.AttributeDefinitionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    AttributeDefinition toEntity(CreateAttributeDefinitionRequest request);

    AttributeDefinitionResponse toResponse(AttributeDefinition entity);
}