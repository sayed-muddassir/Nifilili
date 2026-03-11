package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "ProvinceResponse",
        description = "Province master record exposed to admin clients."
)
public class ProvinceResponse {

    @Schema(description = "Unique identifier of the province.", example = "1")
    private Long id;

    @Schema(description = "Province display name.", example = "Bagmati")
    private String name;
}
