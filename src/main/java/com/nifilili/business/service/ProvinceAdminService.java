package com.nifilili.business.service;

import java.util.List;

import com.nifilili.business.dto.request.CreateProvinceRequest;
import com.nifilili.business.dto.response.ProvinceResponse;

public interface ProvinceAdminService {

    /**
     * Creates a new province master record.
     *
     * @param request province creation payload
     * @return persisted province response
     */
    ProvinceResponse create(CreateProvinceRequest request);

    /**
     * Updates an existing province master record.
     *
     * @param provinceId province identifier
     * @param request province update payload
     * @return updated province response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the province does not exist
     */
    ProvinceResponse update(Long provinceId, CreateProvinceRequest request);

    /**
     * Retrieves one province master record by identifier.
     *
     * @param provinceId province identifier
     * @return matching province response
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the province does not exist
     */
    ProvinceResponse getById(Long provinceId);

    /**
     * Lists all province master records.
     *
     * @return ordered province responses
     */
    List<ProvinceResponse> getAll();

    /**
     * Deletes a province master record when it has no dependent districts.
     *
     * @param provinceId province identifier
     * @throws com.nifilili.core.exception.ResourceNotFoundException when the province does not exist
     * @throws com.nifilili.core.exception.InvalidBusinessStateException when dependent districts exist
     */
    void delete(Long provinceId);
}
