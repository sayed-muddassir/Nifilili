package com.nifilili.kyc.events;

/**
 * Published when an admin approves a business KYC application.
 * Consumed by the business module to mark isKycVerified flag as true.
 */
public record KycApprovedEvent(Long businessId) {
}
