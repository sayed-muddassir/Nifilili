package com.nifilili.core.enums.business;

public enum BusinessStatus {
    DRAFT,        // KYC Rejected
    CLAIM_UNDER_PROGRESS, // Admin-seeded business is being claimed by a user
    PUBLISHED,    // Visible to users
    UNPUBLISHED,  // Hidden by admin
    SUSPENDED     // Abuse or violation
}

