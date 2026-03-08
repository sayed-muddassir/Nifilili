package com.nifilili.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

    private String timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
}
