package com.nifilili.kyc.mapper;

import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;
import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusinessDocumentMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    BusinessDocument toEntity(UploadBusinessDocumentRequest request);

    BusinessDocumentResponse toResponse(BusinessDocument entity);
}
