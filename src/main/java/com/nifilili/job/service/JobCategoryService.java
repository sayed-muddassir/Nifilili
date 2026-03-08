package com.nifilili.job.service;

import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;

import java.util.List;

public interface JobCategoryService {

    /**
     * Creates a new job category. Category names must be unique (case-insensitive).
     *
     * @param request  the category creation request containing the category name
     * @param adminId  the ID of the admin creating the category
     * @return the ID of the newly created job category
     * @throws IllegalArgumentException if a category with the same name already exists
     */
    Long createCategory(CreateJobCategoryRequest request, Long adminId);

    /**
     * Retrieves all job categories.
     *
     * @return list of all job categories with their IDs, names, and creation timestamps
     */
    List<JobCategoryResponse> getAllCategories();
}
