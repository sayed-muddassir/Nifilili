package com.nifilili.kyc.events;

/**
 * Published when an admin approves a business KYC application.
 * Consumed by the business module to transition business status to PUBLISHED.
 */
public record KycApprovedEvent(Long businessId) {
}
