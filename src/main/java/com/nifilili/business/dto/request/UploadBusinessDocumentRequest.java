package com.nifilili.business.dto.request;

import lombok.Data;

@Data
public class UploadBusinessDocumentRequest {

    private Long documentDefinitionId;
    private String fileUrl;
    private String fileName;
}
