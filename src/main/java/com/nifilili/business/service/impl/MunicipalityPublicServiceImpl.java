package com.nifilili.business.service.impl;

import com.nifilili.business.domain.MunicipalityMaster;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.repository.MunicipalityMasterRepository;
import com.nifilili.business.service.MunicipalityPublicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MunicipalityPublicServiceImpl implements MunicipalityPublicService {

    private final MunicipalityMasterRepository municipalityMasterRepository;

    @Override
    public List<MunicipalityResponse> getAll() {
        return municipalityMasterRepository.findAll(Sort.by("name")).stream()
                .map(this::toResponse)
                .toList();
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
