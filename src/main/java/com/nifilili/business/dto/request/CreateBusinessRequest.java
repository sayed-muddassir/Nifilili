package com.nifilili.business.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBusinessRequest {

    @NotNull
    private Long verticalId;

    @NotBlank
    private String name;

    @NotNull
    private Long municipalityId;

    @NotNull
    private Integer wardNumber;

    @NotBlank
    private String toleName;

    @NotBlank
    private String addressField1;

    private String postalCode;

    private String website;
}

