package com.nifilili.kyc.dto.response;

import com.nifilili.core.enums.KycStatus;
import lombok.Data;

import java.util.List;

@Data
public class AdminKycDetailResponse {

    private Long businessId;
    private String businessName;
    private Long verticalId;

    private KycStatus kycStatus;
    private String adminMessage;
    private Integer submissionCount;

    private List<BusinessDocumentResponse> documents;
}
