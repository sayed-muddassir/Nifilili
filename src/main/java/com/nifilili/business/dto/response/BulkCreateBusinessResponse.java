package com.nifilili.business.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
        name = "BulkCreateBusinessResponse",
        description = "Response returned after bulk business creation."
)
public class BulkCreateBusinessResponse {

    private int successCount;
    private int errorCount;
}
