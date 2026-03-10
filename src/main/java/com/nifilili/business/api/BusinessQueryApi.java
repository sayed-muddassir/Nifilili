package com.nifilili.business.api;

import com.nifilili.business.api.model.BusinessMetaData;

public interface BusinessQueryApi {
    BusinessMetaData getBusinessDetailsById(Long businessId);
}
