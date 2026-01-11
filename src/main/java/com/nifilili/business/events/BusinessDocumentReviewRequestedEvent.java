package com.nifilili.business.events;

import com.nifilili.business.dto.request.UploadBusinessDocumentRequest;

public record BusinessDocumentReviewRequestedEvent(Long businessId, UploadBusinessDocumentRequest request) {
}
