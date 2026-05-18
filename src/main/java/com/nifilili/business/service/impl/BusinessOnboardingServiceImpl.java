package com.nifilili.business.service.impl;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.dto.response.UserCreatedBusinessResponse;
import com.nifilili.business.events.BusinessCreatedEvent;
import com.nifilili.business.mapper.BusinessMapper;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.service.BusinessCategoryService;
import com.nifilili.business.service.BusinessOnboardingService;
import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessOnboardingServiceImpl implements BusinessOnboardingService {

    private final ApplicationEventPublisher publisher;

    private final BusinessRepository businessRepository;
    private final BusinessCategoryService businessCategoryService;
    private final BusinessMapper businessMapper;

    /* --------------------------------------------------------
       STEP 1: CREATE BUSINESS
       -------------------------------------------------------- */
    @Override
    @Transactional
    public Long createBusiness(CreateBusinessRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Business business = buildInitialBusinessEntity(request, currentUserId);

        Business persistedBusiness = businessRepository.save(business);

        businessCategoryService.updateCategories(persistedBusiness.getId(), request.getCategoryIds());

        publisher.publishEvent(new BusinessCreatedEvent(business.getId()));

        return business.getId();
    }

    @Override
    public List<UserCreatedBusinessResponse> getLoggedInUserBusinesses(Pageable pageable) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        List<Business> businesses = businessRepository.findByOwnerUserId(currentUserId, pageable)
                .stream()
                .toList();

        if (businesses.isEmpty()) {
            return Collections.emptyList();
        }

        return businesses.stream().map(businessMapper::toDtoResponse).toList();
    }

    private Business buildInitialBusinessEntity(CreateBusinessRequest request, Long ownerUserId) {
        // User-created businesses begin in draft mode and are claimed by their creator.
        return Business.builder()
                .ownerUserId(ownerUserId)
                .verticalId(request.getVerticalId())
                .name(request.getName())
                .legalName(request.getLegalName())
                .municipalityId(request.getMunicipalityId())
                .wardNumber(request.getWardNumber())
                .toleName(request.getToleName())
                .addressField1(request.getAddressField1())
                .addressField2(request.getAddressField2())
                .postalCode(request.getPostalCode())
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .website(request.getWebsite())
                .profileImageUrl(request.getProfileImageUrl())
                .bannerImageUrl(request.getBannerImageUrl())
                .status(BusinessStatus.PUBLISHED)
                .source(BusinessSource.USER_REGISTERED)
                .isClaimed(true)
                .averageRating(BigDecimal.ZERO)
                .reviewCount(0)
                .businessSummary("")
                .registrationDate(Date.valueOf(LocalDate.now()))
                .contacts(request.getContacts())
                .businessHours(request.getBusinessHours())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
