package com.nifilili.order.service.impl;

import com.nifilili.order.model.BusinessConfig;
import com.nifilili.order.service.business.BusinessConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * BusinessConfigService implementation.
 *
 * This is a READ-ONLY service.
 * Safe to cache aggressively.
 */
@Service
public class BusinessConfigServiceImpl implements BusinessConfigService {

    @Override
    public Map<Long, BusinessConfig> getConfigs(Set<Long> businessIds) {

        Map<Long, BusinessConfig> configs = new HashMap<>();

        for (Long businessId : businessIds) {

            /**
             * Mock configuration.
             * Replace with DB or Business module call later.
             */
            BusinessConfig config = BusinessConfig.builder()
                    .businessId(businessId)
                    .taxRate(13)               // 13%
                    .deliveryCharge(100)       // flat
                    .returnWindowDays(7)
                    .build();

            configs.put(businessId, config);
        }

        return configs;
    }
}

