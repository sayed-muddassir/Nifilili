package com.nifilili.order.service.business;

import com.nifilili.order.model.BusinessConfig;

import java.util.Map;
import java.util.Set;

public interface BusinessConfigService {

    /**
     * Fetch configs for multiple businesses.
     *
     * @return Map keyed by businessId
     */
    Map<Long, BusinessConfig> getConfigs(Set<Long> businessIds);
}
