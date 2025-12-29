package com.nifilili.business.dto.response;

import com.nifilili.common.enums.BusinessStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BusinessResponse {

    private Long id;
    private String name;
    private String businessSummary;

    private Long verticalId;
    private List<Long> categoryIds;

    private Long municipalityId;
    private Integer wardNumber;
    private String toleName;
    private String addressField1;
    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private Map<String, Object> contacts;
    private Map<String, Object> businessHours;
    private String website;

    private BusinessStatus status;

    private BigDecimal averageRating;
    private Integer reviewCount;

    private List<SectionResponse> sections;
}
