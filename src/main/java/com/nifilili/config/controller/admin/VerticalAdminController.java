package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateVerticalRequest;
import com.nifilili.config.dto.response.VerticalResponse;
import com.nifilili.config.service.VerticalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/verticals")
@RequiredArgsConstructor
public class VerticalAdminController {

    private final VerticalService service;

    @PostMapping
    public VerticalResponse create(@RequestBody CreateVerticalRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<VerticalResponse> getAllActive() {
        return service.getAllActive();
    }
}
