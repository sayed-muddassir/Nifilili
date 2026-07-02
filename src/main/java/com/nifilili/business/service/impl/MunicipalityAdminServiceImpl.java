package com.nifilili.business.service.impl;

import java.util.List;

import com.nifilili.business.domain.MunicipalityImageMaster;
import com.nifilili.business.repository.MunicipalityImageMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nifilili.business.domain.MunicipalityMaster;
import com.nifilili.business.dto.request.CreateMunicipalityRequest;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.repository.BusinessLocationReferenceRepository;
import com.nifilili.business.repository.DistrictMasterRepository;
import com.nifilili.business.repository.MunicipalityMasterRepository;
import com.nifilili.business.service.MunicipalityAdminService;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MunicipalityAdminServiceImpl implements MunicipalityAdminService {

    private final MunicipalityMasterRepository municipalityMasterRepository;
    private final MunicipalityImageMasterRepository municipalityImageMasterRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final BusinessLocationReferenceRepository businessLocationReferenceRepository;

    @Override
    public MunicipalityResponse create(CreateMunicipalityRequest request) {
        log.info("Creating municipality master record under districtId={}", request.getDistrictId());
        validateDistrictExists(request.getDistrictId());
        MunicipalityMaster municipality = MunicipalityMaster.builder()
                .districtId(request.getDistrictId())
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .build();
        MunicipalityMaster savedMunicipality = municipalityMasterRepository.save(municipality);

        savedMunicipality.setImages(request.getImageUrls() != null ? request.getImageUrls().stream()
                .map(url -> {
                    var image = new com.nifilili.business.domain.MunicipalityImageMaster();
                    image.setMunicipalityId(savedMunicipality.getId());
                    image.setImageUrl(url);
                    return image;
                }).toList() : List.of());

        log.info("Created municipality master record id={}", savedMunicipality.getId());
        return toResponse(savedMunicipality);
    }

    @Override
    public MunicipalityResponse update(Long municipalityId, CreateMunicipalityRequest request) {
        log.info("Updating municipality master record id={}", municipalityId);
        validateDistrictExists(request.getDistrictId());
        MunicipalityMaster municipality = getMunicipalityOrThrow(municipalityId);
        municipality.setDistrictId(request.getDistrictId());
        municipality.setName(request.getName());
        municipality.setType(request.getType());
        municipality.setDescription(request.getDescription());

        municipalityImageMasterRepository.deleteAllByMunicipalityId(municipality.getId());
        List<MunicipalityImageMaster> imageMasterList = municipalityImageMasterRepository.saveAll(request.getImageUrls() != null ? request.getImageUrls().stream()
                .map(url -> {
                    var image = new MunicipalityImageMaster();
                    image.setMunicipalityId(municipality.getId());
                    image.setImageUrl(url);
                    return image;
                }).toList() : List.of());

        municipality.setImages(imageMasterList);

        MunicipalityMaster savedMunicipality = municipalityMasterRepository.save(municipality);
        log.info("Updated municipality master record id={}", savedMunicipality.getId());
        return toResponse(savedMunicipality);
    }

    @Override
    @Transactional(readOnly = true)
    public MunicipalityResponse getById(Long municipalityId) {
        log.info("Fetching municipality master record id={}", municipalityId);
        return toResponse(getMunicipalityOrThrow(municipalityId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MunicipalityResponse> getByDistrict(Long districtId) {
        log.info("Fetching municipality master records for districtId={}", districtId);
        validateDistrictExists(districtId);
        return municipalityMasterRepository.findByDistrictIdOrderByNameAsc(districtId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Long municipalityId) {
        log.info("Deleting municipality master record id={}", municipalityId);
        MunicipalityMaster municipality = getMunicipalityOrThrow(municipalityId);
        if (businessLocationReferenceRepository.existsByMunicipalityId(municipalityId)) {
            throw new InvalidBusinessStateException("Municipality cannot be deleted while businesses reference it");
        }
        municipalityMasterRepository.delete(municipality);
        log.info("Deleted municipality master record id={}", municipalityId);
    }

    private void validateDistrictExists(Long districtId) {
        if (!districtMasterRepository.existsById(districtId)) {
            throw new ResourceNotFoundException("District not found");
        }
    }

    private MunicipalityMaster getMunicipalityOrThrow(Long municipalityId) {
        return municipalityMasterRepository.findById(municipalityId)
                .orElseThrow(() -> new ResourceNotFoundException("Municipality not found"));
    }

    private MunicipalityResponse toResponse(MunicipalityMaster municipality) {
        MunicipalityResponse response = new MunicipalityResponse();
        response.setId(municipality.getId());
        response.setDistrictId(municipality.getDistrictId());
        response.setName(municipality.getName());
        response.setType(municipality.getType());
        response.setDescription(municipality.getDescription());
        response.setImageUrls(municipality.getImages() != null ? municipality.getImages().stream()
                .map(com.nifilili.business.domain.MunicipalityImageMaster::getImageUrl)
                .toList() : List.of());
        return response;
    }
}
