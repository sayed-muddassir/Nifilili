package com.nifilili.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PricingResult {

    private List<PricingItem> items;
    private PricingSummary summary;
}

