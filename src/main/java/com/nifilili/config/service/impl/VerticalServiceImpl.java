package com.nifilili.config.service.impl;

import com.nifilili.config.domain.Vertical;
import com.nifilili.config.dto.request.CreateVerticalRequest;
import com.nifilili.config.dto.response.VerticalResponse;
import com.nifilili.config.mapper.VerticalMapper;
import com.nifilili.config.repository.VerticalRepository;
import com.nifilili.config.service.VerticalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerticalServiceImpl implements VerticalService {

    private final VerticalRepository repository;
    private final VerticalMapper mapper;

    @Override
    public VerticalResponse create(CreateVerticalRequest request) {
        Vertical saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public List<VerticalResponse> getAllActive() {
        return repository.findAll()
                .stream()
                .filter(Vertical::isActive)
                .map(mapper::toResponse)
                .toList();
    }
}
