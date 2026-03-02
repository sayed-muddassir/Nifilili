package com.nifilili.offering.dto.request;

import com.nifilili.core.enums.offering.OfferingStatus;

public record AdminStatusChangeRequest(
        OfferingStatus status
) {}
