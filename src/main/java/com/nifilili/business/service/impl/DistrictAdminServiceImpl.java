package com.nifilili.business.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nifilili.business.domain.DistrictMaster;
import com.nifilili.business.dto.request.CreateDistrictRequest;
import com.nifilili.business.dto.response.DistrictResponse;
import com.nifilili.business.repository.DistrictMasterRepository;
import com.nifilili.business.repository.MunicipalityMasterRepository;
import com.nifilili.business.repository.ProvinceMasterRepository;
import com.nifilili.business.service.DistrictAdminService;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DistrictAdminServiceImpl implements DistrictAdminService {

    private final DistrictMasterRepository districtMasterRepository;
    private final ProvinceMasterRepository provinceMasterRepository;
    private final MunicipalityMasterRepository municipalityMasterRepository;

    @Override
    public DistrictResponse create(CreateDistrictRequest request) {
        log.info("Creating district master record under provinceId={}", request.getProvinceId());
        validateProvinceExists(request.getProvinceId());
        DistrictMaster district = DistrictMaster.builder()
                .provinceId(request.getProvinceId())
                .name(request.getName())
                .build();
        DistrictMaster savedDistrict = districtMasterRepository.save(district);
        log.info("Created district master record id={}", savedDistrict.getId());
        return toResponse(savedDistrict);
    }

    @Override
    public DistrictResponse update(Long districtId, CreateDistrictRequest request) {
        log.info("Updating district master record id={}", districtId);
        validateProvinceExists(request.getProvinceId());
        DistrictMaster district = getDistrictOrThrow(districtId);
        district.setProvinceId(request.getProvinceId());
        district.setName(request.getName());
        DistrictMaster savedDistrict = districtMasterRepository.save(district);
        log.info("Updated district master record id={}", savedDistrict.getId());
        return toResponse(savedDistrict);
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponse getById(Long districtId) {
        log.info("Fetching district master record id={}", districtId);
        return toResponse(getDistrictOrThrow(districtId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistrictResponse> getByProvince(Long provinceId) {
        log.info("Fetching district master records for provinceId={}", provinceId);
        validateProvinceExists(provinceId);
        return districtMasterRepository.findByProvinceIdOrderByNameAsc(provinceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Long districtId) {
        log.info("Deleting district master record id={}", districtId);
        DistrictMaster district = getDistrictOrThrow(districtId);
        if (municipalityMasterRepository.existsByDistrictId(districtId)) {
            throw new InvalidBusinessStateException("District cannot be deleted while municipalities exist");
        }
        districtMasterRepository.delete(district);
        log.info("Deleted district master record id={}", districtId);
    }

    private void validateProvinceExists(Long provinceId) {
        if (!provinceMasterRepository.existsById(provinceId)) {
            throw new ResourceNotFoundException("Province not found");
        }
    }

    private DistrictMaster getDistrictOrThrow(Long districtId) {
        return districtMasterRepository.findById(districtId)
                .orElseThrow(() -> new ResourceNotFoundException("District not found"));
    }

    private DistrictResponse toResponse(DistrictMaster district) {
        DistrictResponse response = new DistrictResponse();
        response.setId(district.getId());
        response.setProvinceId(district.getProvinceId());
        response.setName(district.getName());
        return response;
    }
}
