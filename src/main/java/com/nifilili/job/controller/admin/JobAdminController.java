package com.nifilili.job.controller.admin;

import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;
import com.nifilili.job.service.JobCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/job-categories")
@RequiredArgsConstructor
// TODO ADMIN ACCESS
public class JobAdminController {

    private final JobCategoryService jobCategoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long createCategory(
            @Valid @RequestBody CreateJobCategoryRequest request
    ) {
        // TODO: replace with actual admin ID from security context
        Long adminId = 1L;

        return jobCategoryService.createCategory(request, adminId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<JobCategoryResponse> getAllCategories() {
        return jobCategoryService.getAllCategories();
    }
}
