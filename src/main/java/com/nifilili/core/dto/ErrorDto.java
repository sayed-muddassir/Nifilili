package com.nifilili.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDto {

    private Integer status;
    private String message;
}
