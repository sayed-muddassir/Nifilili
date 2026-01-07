package com.nifilili.business.dto.request;

import lombok.Data;

@Data
public class CreateCategoryRequest {

    private Long businessVerticalId;
    private Long parentCategoryId;

    private String name;
    private String slug;
    private String description;
    private String iconUrl;

    private boolean activeStatus;
}
