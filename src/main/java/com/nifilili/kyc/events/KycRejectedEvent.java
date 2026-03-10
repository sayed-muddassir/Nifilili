package com.nifilili.kyc.events;

/**
 * Published when an admin rejects a business KYC application.
 * Consumed by the business module to transition business status to DRAFT.
 */
public record KycRejectedEvent(Long businessId, String rejectionReason) {
}
