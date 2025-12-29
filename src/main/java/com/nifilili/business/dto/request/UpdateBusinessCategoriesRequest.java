package com.nifilili.business.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateBusinessCategoriesRequest {

    @NotEmpty
    private List<Long> categoryIds;
}

//Validation rules

//All categories must belong to same vertical
//No duplicates
//Categories must be active