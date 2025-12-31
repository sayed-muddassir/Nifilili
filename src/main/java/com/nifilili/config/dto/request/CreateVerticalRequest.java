package com.nifilili.config.dto.request;

import lombok.Data;

@Data
public class CreateVerticalRequest {

    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private boolean isActive;
}
