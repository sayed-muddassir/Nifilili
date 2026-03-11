package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "DistrictResponse",
        description = "District master record exposed to admin clients."
)
public class DistrictResponse {

    @Schema(description = "Unique identifier of the district.", example = "10")
    private Long id;

    @Schema(description = "Parent province identifier.", example = "1")
    private Long provinceId;

    @Schema(description = "District display name.", example = "Kathmandu")
    private String name;
}
