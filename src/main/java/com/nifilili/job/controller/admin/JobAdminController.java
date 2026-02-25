package com.nifilili.job.controller.admin;

import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;
import com.nifilili.job.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/jobs/categories")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ADMIN')")
@Tag(name = "Job Category Management [Admin]", description = "APIs for managing job categories (Admin only)")
public class JobAdminController {

    private final JobCategoryService jobCategoryService;

    @Operation(
            summary = "Create Job Category",
            description = "Creates a new job category in the system. Category names must be unique."
    )
    @ApiResponse(responseCode = "201", description = "Job category created successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed or category already exists")
    @PostMapping
    public ResponseEntity<Long> createCategory(
            @Valid @RequestBody CreateJobCategoryRequest request
    ) {
        // TODO: replace with actual admin ID from security context
        Long adminId = 1L;
        log.info("Create job category request: name='{}'", request.name());
        Long categoryId = jobCategoryService.createCategory(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId);
    }

    @Operation(
            summary = "Get All Job Categories",
            description = "Retrieves all job categories in the system."
    )
    @ApiResponse(responseCode = "200", description = "List of all job categories")
    @GetMapping
    public List<JobCategoryResponse> getAllCategories() {
        log.info("Get all job categories request");
        return jobCategoryService.getAllCategories();
    }
}
