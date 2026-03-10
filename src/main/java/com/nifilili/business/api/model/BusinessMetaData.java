package com.nifilili.business.api.model;

import lombok.Data;

@Data
public class BusinessMetaData {

    private Long businessId;
    private String businessName;
    private Long verticalId;
    private Long ownerUserId;
}
