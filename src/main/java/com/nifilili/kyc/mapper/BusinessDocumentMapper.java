package com.nifilili.kyc.mapper;

import com.nifilili.kyc.domain.BusinessDocument;
import com.nifilili.kyc.dto.response.BusinessDocumentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BusinessDocumentMapper {

    BusinessDocumentResponse toResponse(BusinessDocument entity);
}
