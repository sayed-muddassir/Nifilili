package com.nifilili.business.service.impl;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.dto.response.SectionResponse;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.BusinessQueryService;
import com.nifilili.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessQueryServiceImpl implements BusinessQueryService {

    private final BusinessRepository businessRepository;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final BusinessSectionDataRepository businessDataRepository;

    /* ---------------------------------------------------
       API 1: GET BUSINESS BY ID
       --------------------------------------------------- */
    @Override
    public BusinessResponse getBusinessById(Long businessId) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Business not found"));

        List<Long> categoryIds = businessCategoryRepository
                .findByBusinessId(businessId)
                .stream()
                .map(BusinessCategory::getCategoryId)
                .toList();

        List<SectionResponse> sections =
                buildSectionResponses(businessId);

        return mapToResponse(business, categoryIds, sections);
    }

    /* ---------------------------------------------------
       API 2: GET ALL BUSINESSES (PAGINATED)
       --------------------------------------------------- */
    @Override
    public Page<BusinessResponse> getAllBusinesses(Pageable pageable) {

        return businessRepository.findAll(pageable)
                .map(business -> {
                    List<Long> categoryIds =
                            businessCategoryRepository
                                    .findByBusinessId(business.getId())
                                    .stream()
                                    .map(BusinessCategory::getCategoryId)
                                    .toList();

                    List<SectionResponse> sections =
                            buildSectionResponses(business.getId());

                    return mapToResponse(business, categoryIds, sections);
                });
    }

    /* ---------------------------------------------------
       INTERNAL HELPERS
       --------------------------------------------------- */

    private List<SectionResponse> buildSectionResponses(Long businessId) {

        List<BusinessSectionData> data =
                businessDataRepository.findByBusinessId(businessId);

        Map<Long, List<BusinessSectionData>> grouped =
                data.stream()
                        .collect(Collectors.groupingBy(
                                BusinessSectionData::getSectionId));

        List<SectionResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<BusinessSectionData>> entry : grouped.entrySet()) {

            List<Map<String, Object>> values =
                    entry.getValue()
                            .stream()
                            .map(BusinessSectionData::getFieldValues)
                            .toList();

            responses.add(
                    SectionResponse.builder()
                            .sectionId(entry.getKey())
                            .values(values)
                            .build()
            );
        }

        return responses;
    }

    private BusinessResponse mapToResponse(
            Business business,
            List<Long> categoryIds,
            List<SectionResponse> sections
    ) {

        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .businessSummary(business.getBusinessSummary())
                .verticalId(business.getVerticalId())
                .categoryIds(categoryIds)
                .municipalityId(business.getMunicipalityId())
                .wardNumber(business.getWardNumber())
                .toleName(business.getToleName())
                .addressField1(business.getAddressField1())
                .postalCode(business.getPostalCode())
                .latitude(BigDecimal.valueOf(business.getLatitude()))
                .longitude(BigDecimal.valueOf(business.getLongitude()))
                .contacts(business.getContacts())
                .businessHours(business.getBusinessHours())
                .website(business.getWebsite())
                .status(business.getStatus())
                .averageRating(business.getAverageRating())
                .reviewCount(business.getReviewCount())
                .sections(sections)
                .build();
    }
}
