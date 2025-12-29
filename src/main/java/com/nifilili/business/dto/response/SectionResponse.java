package com.nifilili.business.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SectionResponse {

    private Long sectionId;
    private String sectionName;

    // Each group = one repeatable block
    private List<Map<String, Object>> values;
}
