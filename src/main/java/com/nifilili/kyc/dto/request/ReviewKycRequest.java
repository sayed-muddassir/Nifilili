package com.nifilili.kyc.dto.request;

import lombok.Data;

@Data
public class ReviewKycRequest {

    private boolean approve;
    private String adminMessage;
}
