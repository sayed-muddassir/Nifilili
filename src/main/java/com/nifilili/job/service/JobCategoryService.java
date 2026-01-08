package com.nifilili.job.service;

import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;

import java.util.List;

public interface JobCategoryService {

    Long createCategory(CreateJobCategoryRequest request, Long adminId);

    List<JobCategoryResponse> getAllCategories();
}
