package com.nifilili.config.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {

    private Long id;
    private Long businessVerticalId;
    private Long parentCategoryId;

    private String name;
    private String slug;
    private String description;
    private String iconUrl;

    private boolean activeStatus;
}
