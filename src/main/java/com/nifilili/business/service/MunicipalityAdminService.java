package com.nifilili.business.service;

import java.util.List;

import com.nifilili.business.dto.request.CreateMunicipalityRequest;
import com.nifilili.business.dto.response.MunicipalityResponse;

public interface MunicipalityAdminService {

    /**
     * Creates a new municipality master record under a district.
     *
     * @param request municipality creation payload
     * @return persisted municipality response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the parent district does not exist
     */
    MunicipalityResponse create(CreateMunicipalityRequest request);

    /**
     * Updates an existing municipality master record.
     *
     * @param municipalityId municipality identifier
     * @param request municipality update payload
     * @return updated municipality response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the municipality or parent district does not exist
     */
    MunicipalityResponse update(Long municipalityId, CreateMunicipalityRequest request);

    /**
     * Retrieves one municipality master record by identifier.
     *
     * @param municipalityId municipality identifier
     * @return matching municipality response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the municipality does not exist
     */
    MunicipalityResponse getById(Long municipalityId);

    /**
     * Lists all municipalities for a district.
     *
     * @param districtId district identifier
     * @return ordered municipality responses
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the district does not exist
     */
    List<MunicipalityResponse> getByDistrict(Long districtId);

    /**
     * Deletes a municipality master record when it is not referenced by businesses.
     *
     * @param municipalityId municipality identifier
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the municipality does not exist
     * @throws com.nifilili.core.exception.InvalidBusinessStateException when businesses still reference the municipality
     */
    void delete(Long municipalityId);
}
