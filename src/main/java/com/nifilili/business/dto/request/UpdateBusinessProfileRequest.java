package com.nifilili.business.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class UpdateBusinessProfileRequest {

    private String businessSummary;
    private String legalName;
    private String addressField2;

    private Map<String, Object> contacts;
    private Map<String, Object> businessHours;

    private BigDecimal latitude;
    private BigDecimal longitude;
}
