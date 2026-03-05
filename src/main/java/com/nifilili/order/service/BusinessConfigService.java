package com.nifilili.order.service;

import com.nifilili.order.dto.request.UpdateBusinessConfigRequest;
import com.nifilili.order.dto.response.BusinessConfigResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BusinessConfigService {

    /**
     * Retrieves all configuration key-value pairs for a business.
     *
     * @param businessId the business ID
     * @return list of business config responses
     */
    List<BusinessConfigResponse> getConfigs(Long businessId);

    /**
     * Creates or updates a configuration key-value pair for a business.
     *
     * @param businessId the business ID
     * @param request    the config key and value
     * @return the upserted business config response
     */
    BusinessConfigResponse upsertConfig(Long businessId, UpdateBusinessConfigRequest request);

    /**
     * Retrieves configurations for multiple businesses, keyed by business ID.
     * Used during checkout and order placement to resolve delivery charges, tax rates, etc.
     *
     * @param businessIds the set of business IDs
     * @return map of business ID to their config key-value pairs
     */
    Map<Long, Map<String, String>> getConfigMap(Set<Long> businessIds);
}
