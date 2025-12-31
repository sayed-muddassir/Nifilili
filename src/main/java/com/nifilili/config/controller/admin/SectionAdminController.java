package com.nifilili.config.controller.admin;

import com.nifilili.config.dto.request.CreateSectionFieldRequest;
import com.nifilili.config.dto.request.CreateSectionRequest;
import com.nifilili.config.dto.response.SectionFieldResponse;
import com.nifilili.config.dto.response.SectionResponse;
import com.nifilili.config.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config/sections")
@RequiredArgsConstructor
public class SectionAdminController {

    private final SectionService service;

    @PostMapping
    public SectionResponse createSection(@RequestBody CreateSectionRequest request) {
        return service.createSection(request);
    }

    @PostMapping("/{sectionId}/fields")
    public SectionFieldResponse addField(
            @PathVariable Long sectionId,
            @RequestBody CreateSectionFieldRequest request
    ) {
        return service.addField(sectionId, request);
    }

    @GetMapping("/vertical/{verticalId}")
    public List<SectionResponse> getByVertical(@PathVariable Long verticalId) {
        return service.getByVertical(verticalId);
    }

    @GetMapping("/{sectionId}/fields")
    public List<SectionFieldResponse> getFields(@PathVariable Long sectionId) {
        return service.getFields(sectionId);
    }
}
