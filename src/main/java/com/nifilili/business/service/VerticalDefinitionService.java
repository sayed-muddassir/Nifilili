package com.nifilili.business.service;

import com.nifilili.business.dto.request.CreateVerticalRequest;
import com.nifilili.business.dto.response.VerticalResponse;

import java.util.List;

public interface VerticalDefinitionService {

    List<VerticalResponse> getAll();
    VerticalResponse create(CreateVerticalRequest request);
    VerticalResponse update(Long verticalId, CreateVerticalRequest request);
    List<VerticalResponse> getAllActive();
    void delete(Long verticalId);
}
