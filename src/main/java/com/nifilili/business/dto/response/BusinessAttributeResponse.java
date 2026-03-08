package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
        name = "BusinessAttributeResponse",
        description = "Resolved value for a business attribute."
)
public class BusinessAttributeResponse {

    @Schema(description = "Identifier of the attribute definition.", example = "41")
    private Long attributeId;

    @Schema(description = "Internal or display name of the attribute.", example = "service_modes")
    private String name;

    @Schema(description = "Resolved attribute value. Shape depends on the attribute type and may be a scalar or array.", example = "[\"Delivery Available\",\"Pickup Available\"]", nullable = true)
    private Object attributeValue;
}
