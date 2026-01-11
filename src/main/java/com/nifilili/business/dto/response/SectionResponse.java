package com.nifilili.business.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SectionResponse {

    private Long id;
    private String name;

    // Each group = one repeatable block
    private List<Map<String, Object>> values;
}
