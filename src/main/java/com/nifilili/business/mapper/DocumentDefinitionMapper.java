package com.nifilili.business.mapper;

import com.nifilili.business.domain.DocumentDefinition;
import com.nifilili.business.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.business.dto.response.DocumentDefinitionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentDefinitionMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    DocumentDefinition toEntity(CreateDocumentDefinitionRequest request);

    DocumentDefinitionResponse toResponse(DocumentDefinition entity);
}
