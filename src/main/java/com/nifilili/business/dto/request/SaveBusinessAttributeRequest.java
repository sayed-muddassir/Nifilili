package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "SaveBusinessAttributeRequest",
        description = "Payload used to save a single business attribute value."
)
public class SaveBusinessAttributeRequest {

    @Schema(description = "Identifier of the attribute definition being set for the business.", example = "41", nullable = true)
    private Long attributeId;

    @Schema(description = "Attribute value. Supported shapes depend on the attribute type and may be a boolean, number, string, or list of strings.", example = "[\"Delivery Available\",\"Pickup Available\"]", nullable = true)
    private Object attributeValue;
}
