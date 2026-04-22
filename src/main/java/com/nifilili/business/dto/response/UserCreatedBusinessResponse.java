package com.nifilili.business.dto.response;

import com.nifilili.core.enums.business.BusinessSource;
import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UserCreatedBusinessResponse",
        description = "Detailed business profile returned immediately after a new business is created during onboarding, " +
                "including the assigned business ID and initial status."
)
public class UserCreatedBusinessResponse {

    private Long businessId;
    private Long ownerUserId;
    private Long verticalId;
    private String name;
    private String legalName;
    private Long municipalityId;
    private Integer wardNumber;
    private String toleName;
    private String addressField1;
    private String addressField2;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Map<String, Object> contacts;
    private Map<String, Object> businessHours;
    private String website;
    private String profileImageUrl;
    private String bannerImageUrl;
    private BusinessStatus status;
    private BusinessSource source;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String businessSummary;
    private Date registrationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
