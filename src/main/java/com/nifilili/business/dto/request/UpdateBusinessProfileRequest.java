package com.nifilili.business.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateBusinessProfileRequest {

    private String businessSummary;

    private Map<String, Object> contacts;
    private Map<String, Object> businessHours;

    private Long latitude;
    private Long longitude;
}
