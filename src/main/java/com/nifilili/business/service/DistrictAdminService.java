package com.nifilili.business.service;

import java.util.List;

import com.nifilili.business.dto.request.CreateDistrictRequest;
import com.nifilili.business.dto.response.DistrictResponse;

public interface DistrictAdminService {

    /**
     * Creates a new district master record under a province.
     *
     * @param request district creation payload
     * @return persisted district response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the parent province does not exist
     */
    DistrictResponse create(CreateDistrictRequest request);

    /**
     * Updates an existing district master record.
     *
     * @param districtId district identifier
     * @param request district update payload
     * @return updated district response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the district or parent province does not exist
     */
    DistrictResponse update(Long districtId, CreateDistrictRequest request);

    /**
     * Retrieves one district master record by identifier.
     *
     * @param districtId district identifier
     * @return matching district response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the district does not exist
     */
    DistrictResponse getById(Long districtId);

    /**
     * Lists all districts for a province.
     *
     * @param provinceId province identifier
     * @return ordered district responses
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the province does not exist
     */
    List<DistrictResponse> getByProvince(Long provinceId);

    /**
     * Deletes a district master record when it has no dependent municipalities.
     *
     * @param districtId district identifier
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the district does not exist
     * @throws com.nifilili.core.exception.InvalidBusinessStateException when dependent municipalities exist
     */
    void delete(Long districtId);
}
