package com.nifilili.config.mapper;

import com.nifilili.config.domain.DocumentDefinition;
import com.nifilili.config.dto.request.CreateDocumentDefinitionRequest;
import com.nifilili.config.dto.response.DocumentDefinitionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentDefinitionMapper {

    DocumentDefinition toEntity(CreateDocumentDefinitionRequest request);

    DocumentDefinitionResponse toResponse(DocumentDefinition entity);
}
