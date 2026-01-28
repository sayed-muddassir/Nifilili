package com.nifilili.order.service.business;

import com.nifilili.order.model.PricingContext;
import com.nifilili.order.model.PricingResult;

public interface PricingService {
    PricingResult calculate(PricingContext context);
}

