package com.nifilili.job.controller.admin;

import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;
import com.nifilili.job.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/job-categories")
@RequiredArgsConstructor
// TODO ADMIN ACCESS
@Tag(name = "Job Category Management [Admin]", description = "APIs for managing job categories (Admin only)")
public class JobAdminController {

    private final JobCategoryService jobCategoryService;

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Job Category",
            description = "Creates a new job category in the system."
    )
    public Long createCategory(
            @Valid @RequestBody CreateJobCategoryRequest request
    ) {
        // TODO: replace with actual admin ID from security context
        Long adminId = 1L;

        return jobCategoryService.createCategory(request, adminId);
    }

    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Job Categories",
            description = "Retrieves all job categories in the system."
    )
    public List<JobCategoryResponse> getAllCategories() {
        return jobCategoryService.getAllCategories();
    }
}
