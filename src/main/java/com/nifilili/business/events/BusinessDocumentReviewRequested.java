package com.nifilili.business.events;

import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;

public record BusinessDocumentReviewRequested(Long businessId, UploadBusinessDocumentRequest request) {
}
