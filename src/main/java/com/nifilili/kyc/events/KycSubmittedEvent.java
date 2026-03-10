package com.nifilili.kyc.events;

/**
 * Published when a business owner submits their KYC application for admin review.
 * Consumed by the business module to transition business status to PENDING.
 */
public record KycSubmittedEvent(Long businessId) {
}
