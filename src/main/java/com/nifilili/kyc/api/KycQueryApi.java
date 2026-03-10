package com.nifilili.kyc.api;

/**
 * Cross-module API for querying KYC verification status.
 * Other modules can inject this interface to check if a business is verified
 * without depending on KYC internals.
 */
public interface KycQueryApi {

    /**
     * Checks whether a business has been KYC-verified (approved).
     *
     * @param businessId the business to check
     * @return true if KYC status is APPROVED, false otherwise
     */
    boolean isBusinessVerified(Long businessId);
}
