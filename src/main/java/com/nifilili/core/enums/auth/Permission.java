package com.nifilili.core.enums.auth;

/**
 * All granular permissions in the system, seeded in V12 migration.
 * Used with {@code @PreAuthorize("hasAuthority('PERMISSION_NAME')")}.
 */
public enum Permission {

    // Business module
    BUSINESS_CREATE,
    BUSINESS_MANAGE_OWN,
    BUSINESS_MANAGE_ALL,
    BUSINESS_VIEW_ALL,

    // KYC module
    KYC_SUBMIT,
    KYC_REVIEW,

    // Job module
    JOB_CREATE,
    JOB_MANAGE_OWN,
    JOB_APPLY,
    JOB_MANAGE_ALL,

    // Offering module
    OFFERING_CREATE,
    OFFERING_MANAGE_OWN,
    OFFERING_MANAGE_ALL,

    // Order module
    ORDER_PLACE,
    ORDER_MANAGE_OWN,
    ORDER_MANAGE_BUSINESS,
    ORDER_MANAGE_ALL,

    // Quote module
    QUOTE_REQUEST,
    QUOTE_RESPOND,
    QUOTE_MANAGE_ALL,

    // Account module
    ACCOUNT_MANAGE_OWN,
    ACCOUNT_MANAGE_ALL,

    // System
    SYSTEM_ADMIN,
    SUPPORT_MANAGE
}
