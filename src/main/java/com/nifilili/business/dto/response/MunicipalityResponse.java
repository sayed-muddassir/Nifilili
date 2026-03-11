package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
}
