package com.nifilili.kyc.dto.response;

import com.nifilili.core.enums.business.DocumentStatus;
import lombok.Data;

@Data
public class BusinessDocumentResponse {

    private Long id;
    private Long documentDefinitionId;

    private String fileName;
    private String fileUrl;
    private DocumentStatus status;
    private String rejectionReason;
}
