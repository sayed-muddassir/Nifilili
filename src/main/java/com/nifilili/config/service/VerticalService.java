package com.nifilili.config.service;

import com.nifilili.config.dto.request.CreateVerticalRequest;
import com.nifilili.config.dto.response.VerticalResponse;

import java.util.List;

public interface VerticalService {

    VerticalResponse create(CreateVerticalRequest request);

    List<VerticalResponse> getAllActive();
}
