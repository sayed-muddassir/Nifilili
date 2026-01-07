package com.nifilili.business.service.impl;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.request.SaveSectionDataRequest;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.BusinessSectionService;
import com.nifilili.business.validation.SectionValidationService;
import com.nifilili.core.enums.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessSectionServiceImpl implements BusinessSectionService {

    private final BusinessRepository businessRepository;
    private final BusinessSectionDataRepository businessSectionDataRepository;

    private final SectionGroupRepository sectionGroupRepository;
    private final SectionRepository sectionRepository;
    private final SectionFieldRepository sectionFieldRepository;

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

        Long groupId = request.getSectionGroupId();

        if (groupId == null && sectionDefinition.isAllowMultiple()) {
            BusinessSectionGroup group = new BusinessSectionGroup(
                    businessId,
                    sectionId,
                    "Group"
            );
            sectionGroupRepository.save(group);
            groupId = group.getId();
        }

        BusinessSectionData data = new BusinessSectionData(
                businessId,
                sectionId,
                groupId,
                request.getFieldValues()
        );

        businessSectionDataRepository.save(data);
    }

    private Business loadOwnedBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Business not found"));

//        Long userId = SecurityUtil.getCurrentUserId();
//        if (!business.getOwnerId().equals(userId)) {
//            throw new InvalidBusinessStateException("Unauthorized access");
//        }
        return business;
    }

    private void ensureEditable(Business business) {
        if (business.getStatus() == BusinessStatus.PENDING
                || business.getStatus() == BusinessStatus.PUBLISHED) {
            throw new InvalidBusinessStateException(
                    "Business cannot be edited in current state");
        }
    }

    private void validateSectionApplicability(Business business, SectionDefinition sectionDefinition) {
        if (!sectionDefinition.getVerticalId().equals(business.getVerticalId())) {
            throw new InvalidBusinessStateException(
                    "SectionDefinition does not apply to this business");
        }
    }
}
