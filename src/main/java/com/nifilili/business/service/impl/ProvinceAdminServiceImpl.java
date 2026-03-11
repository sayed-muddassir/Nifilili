package com.nifilili.business.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nifilili.business.domain.ProvinceMaster;
import com.nifilili.business.dto.request.CreateProvinceRequest;
import com.nifilili.business.dto.response.ProvinceResponse;
import com.nifilili.business.repository.DistrictMasterRepository;
import com.nifilili.business.repository.ProvinceMasterRepository;
import com.nifilili.business.service.ProvinceAdminService;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProvinceAdminServiceImpl implements ProvinceAdminService {

    private final ProvinceMasterRepository provinceMasterRepository;
    private final DistrictMasterRepository districtMasterRepository;

    @Override
    public ProvinceResponse create(CreateProvinceRequest request) {
        log.info("Creating province master record with name={}", request.getName());
        ProvinceMaster province = ProvinceMaster.builder()
                .name(request.getName())
                .build();
        ProvinceMaster savedProvince = provinceMasterRepository.save(province);
        log.info("Created province master record with id={}", savedProvince.getId());
        return toResponse(savedProvince);
    }

    @Override
    public ProvinceResponse update(Long provinceId, CreateProvinceRequest request) {
        log.info("Updating province master record id={}", provinceId);
        ProvinceMaster province = getProvinceOrThrow(provinceId);
        province.setName(request.getName());
        ProvinceMaster savedProvince = provinceMasterRepository.save(province);
        log.info("Updated province master record id={}", savedProvince.getId());
        return toResponse(savedProvince);
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinceResponse getById(Long provinceId) {
        log.info("Fetching province master record id={}", provinceId);
        return toResponse(getProvinceOrThrow(provinceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceResponse> getAll() {
        log.info("Fetching all province master records");
        return provinceMasterRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Long provinceId) {
        log.info("Deleting province master record id={}", provinceId);
        ProvinceMaster province = getProvinceOrThrow(provinceId);
        if (districtMasterRepository.existsByProvinceId(provinceId)) {
            throw new InvalidBusinessStateException("Province cannot be deleted while districts exist");
        }
        provinceMasterRepository.delete(province);
        log.info("Deleted province master record id={}", provinceId);
    }

    private ProvinceMaster getProvinceOrThrow(Long provinceId) {
        return provinceMasterRepository.findById(provinceId)
                .orElseThrow(() -> new ResourceNotFoundException("Province not found"));
    }

    private ProvinceResponse toResponse(ProvinceMaster province) {
        ProvinceResponse response = new ProvinceResponse();
        response.setId(province.getId());
        response.setName(province.getName());
        return response;
    }
}
