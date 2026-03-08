package com.nifilili.business.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "UpdateBusinessCategoriesRequest",
        description = "Payload used to assign or replace the active business categories for a business."
)
public class UpdateBusinessCategoriesRequest {

    @ArraySchema(
            arraySchema = @Schema(description = "List of category identifiers to attach to the business.", requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(description = "Business category identifier.", example = "11")
    )
    @NotEmpty
    private List<Long> categoryIds;
}

//Validation rules

//All categories must belong to same vertical
//No duplicates
//Categories must be active
