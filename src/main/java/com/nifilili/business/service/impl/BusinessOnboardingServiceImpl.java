package com.nifilili.business.service.impl;

import com.nifilili.business.domain.*;
import com.nifilili.business.dto.request.CreateBusinessRequest;
import com.nifilili.business.events.BusinessCreated;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.BusinessOnboardingService;
import com.nifilili.core.enums.BusinessSource;
import com.nifilili.core.enums.BusinessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessOnboardingServiceImpl implements BusinessOnboardingService {

    private final ApplicationEventPublisher publisher;

    private final BusinessRepository businessRepository;

    /* --------------------------------------------------------
       STEP 1: CREATE BUSINESS
       -------------------------------------------------------- */
    @Override
    public Long createBusiness(CreateBusinessRequest request) {

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
                .isClaimed(true)
                .averageRating(BigDecimal.valueOf(0))
                .reviewCount(0)
                .businessSummary("")
                .registrationDate(Date.valueOf(LocalDate.now()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        businessRepository.save(business);

        publisher.publishEvent(new BusinessCreated(business.getId()));

        return business.getId();
    }
}

