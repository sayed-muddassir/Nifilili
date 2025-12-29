package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.domain.BusinessCategory;
import com.nifilili.business.domain.BusinessSectionData;
import com.nifilili.business.domain.SectionGroup;
import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.dto.request.SaveSectionDataRequest;
import com.nifilili.business.dto.request.UpdateBusinessProfileRequest;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.BusinessOnboardingService;
import com.nifilili.common.enums.BusinessSource;
import com.nifilili.common.enums.BusinessStatus;
import com.nifilili.common.enums.KycStatus;
import com.nifilili.common.exception.InvalidBusinessStateException;
import com.nifilili.common.exception.ResourceNotFoundException;
//import com.nifilili.common.security.SecurityUtil;
import com.nifilili.config.domain.Section;
import com.nifilili.config.domain.SectionField;
import com.nifilili.config.repository.SectionFieldRepository;
import com.nifilili.config.repository.SectionRepository;
import com.nifilili.kyc.service.BusinessKycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessOnboardingServiceImpl implements BusinessOnboardingService {

    private final BusinessRepository businessRepository;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final BusinessSectionDataRepository businessSectionDataRepository;

    private final SectionGroupRepository sectionGroupRepository;
    private final SectionRepository sectionRepository;
    private final SectionFieldRepository sectionFieldRepository;

    private final BusinessKycService businessKycService;

    /* --------------------------------------------------------
       STEP 1: CREATE BUSINESS
       -------------------------------------------------------- */
    @Override
    public Long createBusiness(CreateBusinessRequest request) {

//        Long userId = SecurityUtil.getCurrentUserId();

        Business business = Business.builder()
                .verticalId(request.getVerticalId())
                .name(request.getName())
                .municipalityId(request.getMunicipalityId())
                .wardNumber(request.getWardNumber())
                .toleName(request.getToleName())
                .addressField1(request.getAddressField1())
                .postalCode(request.getPostalCode())
                .latitude(0L) // Default to 0, to be updated later
                .longitude(0L) // Default to 0, to be updated later
                .contacts(Map.of())
                .businessHours(Map.of())
                .website(request.getWebsite())
                .status(BusinessStatus.DRAFT)
                .source(BusinessSource.USER_REGISTERED)
//                .isClaimed(true) TODO: Change isClaimed later
                .averageRating(BigDecimal.valueOf(0))
                .reviewCount(0)
                .businessSummary("")
                .registrationDate(Date.valueOf(LocalDate.now()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        businessRepository.save(business);
        return business.getId();
    }

    /* --------------------------------------------------------
       STEP 2: UPDATE CORE PROFILE
       -------------------------------------------------------- */
    @Override
    public void updateProfile(Long businessId, UpdateBusinessProfileRequest request) {

        Business business = loadOwnedBusiness(businessId);

        ensureEditable(business);

        business.setBusinessSummary(request.getBusinessSummary());
//        business.setContacts(request.getContacts());
//        business.setBusinessHours(request.getBusinessHours());
        business.setLatitude(request.getLatitude());
        business.setLongitude(request.getLongitude());

        businessRepository.save(business);
    }

    /* --------------------------------------------------------
       STEP 3: UPDATE CATEGORIES
       -------------------------------------------------------- */
    @Override
    public void updateCategories(Long businessId, List<Long> categoryIds) {

        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);

        businessCategoryRepository.deleteByBusinessId(businessId);

        for (Long categoryId : categoryIds) {
            BusinessCategory mapping = new BusinessCategory(
                    businessId,
                    categoryId
            );
            businessCategoryRepository.save(mapping);
        }
    }

    /* --------------------------------------------------------
       STEP 4: SAVE SECTION DATA
       -------------------------------------------------------- */
    @Override
    public void saveSectionData(
            Long businessId,
            Long sectionId,
            SaveSectionDataRequest request
    ) {

        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Section not found"));

        validateSectionApplicability(business, section);

        List<SectionField> fields =
                sectionFieldRepository.findBySectionId(sectionId);

        validateSectionFields(fields, request.getFieldValues());

        Long groupId = request.getSectionGroupId();

        if (groupId == null && section.isAllowMultiple()) {
            SectionGroup group = new SectionGroup(
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

    /* --------------------------------------------------------
       STEP 5: SUBMIT FOR KYC
       -------------------------------------------------------- */
    @Override
    public void submitForKyc(Long businessId) {

        Business business = loadOwnedBusiness(businessId);
        ensureEditable(business);

        // Delegated checks
        businessKycService.validateSubmissionEligibility(businessId);

        business.setStatus(BusinessStatus.PENDING);
        businessRepository.save(business);

        businessKycService.createOrUpdateKyc(businessId, KycStatus.PENDING);
    }

    /* --------------------------------------------------------
       INTERNAL HELPERS
       -------------------------------------------------------- */

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

    private void validateSectionApplicability(Business business, Section section) {
        if (!section.getVerticalId().equals(business.getVerticalId())) {
            throw new InvalidBusinessStateException(
                    "Section does not apply to this business");
        }
    }

    private void validateSectionFields(
            List<SectionField> fields,
            Map<String, Object> values
    ) {
        for (SectionField field : fields) {
            if (field.isRequired()
                    && !values.containsKey(field.getName())) {
                throw new InvalidBusinessStateException(
                        "Missing required field: " + field.getName());
            }
        }
    }
}

