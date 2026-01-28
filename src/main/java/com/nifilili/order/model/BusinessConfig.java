package com.nifilili.order.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Snapshot of business operational settings.
 */
@Getter
@Builder
public class BusinessConfig {

    private final Long businessId;
    private final int taxRate;           // percentage
    private final int deliveryCharge;    // flat
    private final int returnWindowDays;
}

