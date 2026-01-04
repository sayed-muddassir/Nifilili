package com.nifilili.kyc.dto.response;

import lombok.Data;

@Data
public class BusinessDocumentResponse {

    private Long id;
    private Long documentDefinitionId;

    private String fileName;
    private String fileUrl;

    private String status;
    private String rejectionReason;
}
