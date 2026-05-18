package com.nifilili.business.service.impl;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.request.SaveSectionDataRequest;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.BusinessSectionService;
import com.nifilili.business.validation.SectionValidationService;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessSectionServiceImpl implements BusinessSectionService {

    private final BusinessRepository businessRepository;
    private final BusinessSectionDataRepository businessSectionDataRepository;

    private final SectionGroupRepository sectionGroupRepository;
    private final SectionRepository sectionRepository;
    private final SectionFieldRepository sectionFieldRepository;
    private final BusinessCategoryRepository businessCategoryRepository;

    private final SectionValidationService sectionValidationService;

    @Override
    public void saveSectionData(
            Long businessId,
            Long sectionId,
            SaveSectionDataRequest request
    ) {

        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);

        SectionDefinition sectionDefinition = sectionRepository.findById(sectionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("SectionDefinition not found"));

        validateSectionApplicability(business, sectionDefinition);

        List<SectionField> fields =
                sectionFieldRepository.findBySectionId(sectionId);

        sectionValidationService.validate(fields, request.getFieldValues());

        Long sectionGroupId = resolveSectionGroupIdForSave(businessId, sectionId, sectionDefinition, request.getSectionGroupId());

        BusinessSectionData data = new BusinessSectionData(
                businessId,
                sectionId,
                sectionGroupId,
                request.getFieldValues()
        );

        businessSectionDataRepository.save(data);
    }

    private Business loadOwnedBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Business not found"));

        Long authenticatedUserId = SecurityUtil.getCurrentUserId();
        if (business.getOwnerUserId() == null || !business.getOwnerUserId().equals(authenticatedUserId)) {
            throw new InvalidBusinessStateException("Unauthorized access");
        }
        return business;
    }

    private void ensureEditable(Business business) {
        if (business.getStatus() == BusinessStatus.DRAFT) {
            throw new InvalidBusinessStateException(
                    "Business cannot be edited in current state");
        }
    }

    private void validateSectionApplicability(Business business, SectionDefinition sectionDefinition) {
        if (!sectionDefinition.getVerticalId().equals(business.getVerticalId())) {
            throw new InvalidBusinessStateException(
                    "SectionDefinition does not apply to this business");
        }

        if (sectionDefinition.getCategoryId() != null) {
            Set<Long> assignedCategoryIds = businessCategoryRepository.findByBusinessId(business.getId())
                    .stream()
                    .map(BusinessCategory::getCategoryId)
                    .collect(Collectors.toSet());

            if (!assignedCategoryIds.contains(sectionDefinition.getCategoryId())) {
                throw new InvalidBusinessStateException("Section category is not assigned to this business");
            }
        }
    }

    private Long resolveSectionGroupIdForSave(
            Long businessId,
            Long sectionId,
            SectionDefinition sectionDefinition,
            Long requestedSectionGroupId
    ) {
        if (sectionDefinition.isAllowMultiple()) {
            if (requestedSectionGroupId != null) {
                return requestedSectionGroupId;
            }

            // Repeatable sections require a concrete group so entries stay logically grouped.
            BusinessSectionGroup group = new BusinessSectionGroup(
                    businessId,
                    sectionId,
                    "Auto Group"
            );
            sectionGroupRepository.save(group);
            return group.getId();
        }

        if (requestedSectionGroupId != null) {
            throw new InvalidBusinessStateException("Non-repeatable sections cannot use sectionGroupId");
        }

        // Non-repeatable sections intentionally persist a null sectionGroupId.
        return null;
    }
}
