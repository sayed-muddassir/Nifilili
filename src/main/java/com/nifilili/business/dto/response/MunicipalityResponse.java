package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "MunicipalityResponse",
        description = "Municipality master record exposed to admin clients."
)
public class MunicipalityResponse {

    @Schema(description = "Unique identifier of the municipality.", example = "101")
    private Long id;

    @Schema(description = "Parent district identifier.", example = "10")
    private Long districtId;

    @Schema(description = "Municipality display name.", example = "Kathmandu Metropolitan City")
    private String name;

    @Schema(description = "Municipality type.", example = "metropolitan")
    private String type;

    @Schema(description = "Municipality description.", example = "Kathmandu is the capital city of Nepal.")
    private String description;

    @Schema(description = "List of image URLs associated with the municipality.", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> imageUrls;
}
