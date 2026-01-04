package com.nifilili.kyc.service;

import com.nifilili.kyc.dto.request.ReviewKycRequest;
import com.nifilili.kyc.dto.request.UploadBusinessDocumentRequest;

public interface KycService {

    void uploadDocument(Long businessId, UploadBusinessDocumentRequest request);

    void submit(Long businessId, String message);

    void review(Long businessId, ReviewKycRequest request);
}
