package com.nifilili.core.dto;

import lombok.Data;

@Data
public class AddressDto {
    private Long municipalityId;
    private Integer wardNumber;
    private String toleName;
    private String addressField1;
    private String postalCode;
}
