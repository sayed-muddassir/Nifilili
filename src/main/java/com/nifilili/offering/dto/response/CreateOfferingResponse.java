package com.nifilili.offering.dto.response;

import com.nifilili.core.enums.offering.OfferingStatus;

import java.time.LocalDateTime;

public record CreateOfferingResponse(
        Long id,
        OfferingStatus status,
        LocalDateTime createdAt
) {}

