package com.nifilili.business.events;

/**
 * Published when a user claims an admin-seeded business listing.
 * Consumed by KYC module to create an initial KYC record.
 */
public record BusinessClaimedEvent(Long businessId, Long claimedByUserId) {
}
