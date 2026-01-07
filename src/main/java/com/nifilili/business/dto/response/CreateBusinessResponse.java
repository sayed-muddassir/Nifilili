package com.nifilili.business.dto.response;

import com.nifilili.core.enums.BusinessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateBusinessResponse {

    private Long businessId;
    private BusinessStatus status; // DRAFT
    private String message;
}

