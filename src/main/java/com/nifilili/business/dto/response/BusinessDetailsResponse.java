package com.nifilili.business.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessDetailsResponse {

    private String verticalName;
    private List<String> categoryNames;
    private String municipalityName;
}
