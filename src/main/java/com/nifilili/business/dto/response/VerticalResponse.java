package com.nifilili.business.dto.response;

import lombok.Data;

@Data
public class VerticalResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private boolean isActive;
}
