package com.nifilili.business.dto.response;

import com.nifilili.core.enums.business.BusinessStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(
        name = "BusinessResponse",
        description = "Detailed business profile returned during onboarding, profile management, and public discovery flows."
)
public class BusinessResponse {

    @Schema(description = "Unique identifier of the business.", example = "501")
    private Long id;

    @Schema(description = "Public-facing business name.", example = "Nifilili Crafts")
    private String name;

    @Schema(description = "Registered legal name of the business.", example = "Nifilili Crafts Private Limited", nullable = true)
    private String legalName;

    @Schema(description = "Short summary of the business.", example = "Handcrafted decor and curated gift products.", nullable = true)
    private String businessSummary;

    @Schema(description = "Identifier of the vertical assigned to the business.", example = "1")
    private Long verticalId;

    @ArraySchema(
            arraySchema = @Schema(description = "Category identifiers currently attached to the business.", nullable = true),
            schema = @Schema(example = "11")
    )
    private List<Long> categoryIds;

    @Schema(description = "Municipality identifier for the business address.", example = "101")
    private Long municipalityId;

    @Schema(description = "Ward number for the business address.", example = "5")
    private Integer wardNumber;

    @Schema(description = "Tole or locality name.", example = "Putalisadak")
    private String toleName;

    @Schema(description = "Primary address line.", example = "Bagbazar Main Road")
    private String addressField1;

    @Schema(description = "Secondary address line or landmark.", example = "2nd Floor, Opposite City Mall", nullable = true)
    private String addressField2;

    @Schema(description = "Postal code for the business location.", example = "44600", nullable = true)
    private String postalCode;

    @Schema(description = "Latitude coordinate of the business location.", example = "27.7172", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "Longitude coordinate of the business location.", example = "85.3240", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "Contact metadata such as phone, email, or social handles.", example = "{\"phone\":\"+977-9800000000\",\"email\":\"hello@nifilili.com\"}", nullable = true)
    private Map<String, Object> contacts;

    @Schema(description = "Business operating hours keyed by day or schedule block.", example = "{\"monday\":\"09:00-18:00\"}", nullable = true)
    private Map<String, Object> businessHours;

    @Schema(description = "Official website URL for the business.", example = "https://www.nifilili.com", nullable = true)
    private String website;

    @Schema(description = "Current lifecycle status of the business.", example = "PENDING")
    private BusinessStatus status;

    @Schema(description = "Average customer rating for the business.", example = "4.6", nullable = true)
    private BigDecimal averageRating;

    @Schema(description = "Total number of reviews contributing to the rating.", example = "128", nullable = true)
    private Integer reviewCount;

    @ArraySchema(
            arraySchema = @Schema(description = "Structured onboarding section values for the business.", nullable = true),
            schema = @Schema(implementation = SectionResponse.class)
    )
    private List<SectionResponse> sections;

    @ArraySchema(
            arraySchema = @Schema(description = "Resolved business attribute values.", nullable = true),
            schema = @Schema(implementation = BusinessAttributeResponse.class)
    )
    private List<BusinessAttributeResponse> attributes;

    @Schema(description = "KYC verification status of the business.", example = "true")
    private boolean isKycVerified;

    @Schema(description = "URL of the business profile image.", example = "https://example.com/profile.jpg", nullable = true)
    private String profileImageUrl;

    @Schema(description = "URL of the business banner image.", example = "https://example.com/banner.jpg", nullable = true)
    private String bannerImageUrl;

    @Schema(description = "Detailed information about the business.",
            example = "{\"verticalName\":\"Retail\",\"categoryNames\":[\"Clothing\",\"Accessories\"]," +
                    "\"municipalityName\":\"Kathmandu\",\"wardName\":\"Ward 1\"}",
            nullable = true)
    private BusinessDetailsResponse businessDetails;
}
