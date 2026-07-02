package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.domain.BusinessAttribute;
import com.nifilili.business.domain.BusinessCategory;
import com.nifilili.business.domain.BusinessSectionData;
import com.nifilili.business.dto.response.BusinessAttributeResponse;
import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.dto.response.SectionResponse;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.BusinessQueryService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BusinessQueryServiceImpl implements BusinessQueryService {

    private final BusinessRepository businessRepository;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final BusinessSectionDataRepository businessDataRepository;
    private final BusinessAttributeRepository businessAttributeRepository;

    private final SectionRepository sectionRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;

    @Override
    public BusinessResponse getBusinessById(Long businessId) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Business not found"));

        if (!BusinessStatus.PUBLISHED.equals(business.getStatus())) {
            throw new ResourceNotFoundException("Business not published yet");
        }
        List<Long> categoryIds = businessCategoryRepository
                .findByBusinessId(businessId)
                .stream()
                .map(BusinessCategory::getCategoryId)
                .toList();

        List<SectionResponse> sections =
                buildSectionResponses(businessId);

        List<BusinessAttributeResponse> attributes =
                buildAttributeResponses(businessId);

        return mapToResponse(business, categoryIds, sections, attributes);
    }

    @Override
    public Page<BusinessResponse> getAllBusinessesByStatus(Pageable pageable, BusinessStatus businessStatus) {
        return businessRepository.findByStatus(businessStatus, pageable)
                .map(business -> {
                    List<Long> categoryIds =
                            businessCategoryRepository
                                    .findByBusinessId(business.getId())
                                    .stream()
                                    .map(BusinessCategory::getCategoryId)
                                    .toList();

                    List<SectionResponse> sections =
                            buildSectionResponses(business.getId());

                    List<BusinessAttributeResponse> attributes =
                            buildAttributeResponses(business.getId());

                    return mapToResponse(business, categoryIds, sections, attributes);
                });
    }

    @Override
    public Page<BusinessResponse> getAllBusinessesByClaimUnderProgress(Pageable pageable, BusinessStatus businessStatus) {
        Long userId = SecurityUtil.getCurrentUserId();
        return businessRepository.findByClaimedByUserIdAndStatus(userId, businessStatus, pageable)
                .map(business -> {
                    List<Long> categoryIds =
                            businessCategoryRepository
                                    .findByBusinessId(business.getId())
                                    .stream()
                                    .map(BusinessCategory::getCategoryId)
                                    .toList();

                    List<SectionResponse> sections =
                            buildSectionResponses(business.getId());

                    List<BusinessAttributeResponse> attributes =
                            buildAttributeResponses(business.getId());

                    return mapToResponse(business, categoryIds, sections, attributes);
                });
    }

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
                            .id(entry.getKey())
                            .name(sectionRepository.findById(entry.getKey())
                                    .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + entry.getKey()))
                                    .getName())
                            .values(values)
                            .build()
            );
        }

        return responses;
    }

    private List<BusinessAttributeResponse> buildAttributeResponses(Long businessId) {
        List<BusinessAttribute> data =
                businessAttributeRepository.findByBusinessId(businessId);

        List<BusinessAttributeResponse> responses = new ArrayList<>();

        for (BusinessAttribute attribute : data) {
            responses.add(
                    BusinessAttributeResponse.builder()
                            .attributeId(attribute.getAttributeId())
                            .name(attributeDefinitionRepository.findById(attribute.getAttributeId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Attribute not found: " + attribute.getAttributeId()))
                                    .getName())
                            .attributeValue(attribute.getAttributeValue())
                            .build(
                            ));
        }

        return responses;
    }

    @Override
    public List<BusinessResponse> getTopNByVerticalAndMunicipality(Long verticalId, Long municipalityId, int limit) {
        if (verticalId == null || verticalId <= 0) {
            throw new IllegalArgumentException("verticalId must be a positive number");
        }
        if (municipalityId == null || municipalityId <= 0) {
            throw new IllegalArgumentException("municipalityId must be a positive number");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }

        log.debug("Fetching top {} businesses for verticalId={}, municipalityId={}", limit, verticalId, municipalityId);

        List<Business> businesses = businessRepository.findTopNByVerticalAndMunicipalityAndStatus(
            verticalId, municipalityId, limit
        );

        log.info("Found {} published businesses for verticalId={}, municipalityId={}", businesses.size(), verticalId, municipalityId);

        return businesses.stream()
            .map(business -> {
                List<Long> categoryIds = businessCategoryRepository
                    .findByBusinessId(business.getId())
                    .stream()
                    .map(BusinessCategory::getCategoryId)
                    .toList();

                List<SectionResponse> sections = buildSectionResponses(business.getId());
                List<BusinessAttributeResponse> attributes = buildAttributeResponses(business.getId());

                return mapToResponse(business, categoryIds, sections, attributes);
            })
            .toList();
    }

    private BusinessResponse mapToResponse (Business business,
            List<Long> categoryIds,
            List<SectionResponse> sections,
            List<BusinessAttributeResponse> attributes
    ) {

        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .legalName(business.getLegalName())
                .businessSummary(business.getBusinessSummary())
                .verticalId(business.getVerticalId())
                .categoryIds(categoryIds)
                .municipalityId(business.getMunicipalityId())
                .wardNumber(business.getWardNumber())
                .toleName(business.getToleName())
                .addressField1(business.getAddressField1())
                .addressField2(business.getAddressField2())
                .postalCode(business.getPostalCode())
                .latitude(business.getLatitude() != null ? business.getLatitude() : BigDecimal.ZERO)
                .longitude(business.getLongitude() != null ? business.getLongitude() : BigDecimal.ZERO)
                .contacts(business.getContacts())
                .businessHours(business.getBusinessHours())
                .website(business.getWebsite())
                .status(business.getStatus())
                .averageRating(business.getAverageRating())
                .reviewCount(business.getReviewCount())
                .sections(sections)
                .attributes(attributes)
                .isKycVerified(business.isKycVerified())
                .build();
    }
}
