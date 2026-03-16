package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LinkEmailRequest", description = "Request to initiate email address linking.")
public class LinkEmailRequest {

    @Schema(description = "Email address to link to the account.", example = "john.doe@nifilili.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
