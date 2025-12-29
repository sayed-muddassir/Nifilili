package com.nifilili.common.enums;

public enum BusinessStatus {
    DRAFT,        // User-created, not submitted
    PENDING,      // Waiting for admin/KYC
    PUBLISHED,    // Visible to users
    UNPUBLISHED,  // Hidden by admin
    SUSPENDED     // Abuse or violation
}

