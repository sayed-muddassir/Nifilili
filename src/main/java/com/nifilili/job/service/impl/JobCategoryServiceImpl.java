package com.nifilili.job.service.impl;

import com.nifilili.job.domain.JobCategory;
import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;
import com.nifilili.job.repository.JobCategoryRepository;
import com.nifilili.job.service.JobCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    @Override
    @Transactional
    public Long createCategory(CreateJobCategoryRequest request, Long adminId) {
        log.info("Creating job category name='{}' by adminId='{}'", request.name(), adminId);

        if (jobCategoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Job category already exists");
        }

        JobCategory category = new JobCategory(request.name(), adminId);
        jobCategoryRepository.save(category);

        log.info("Job category id='{}' created successfully", category.getId());
        return category.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobCategoryResponse> getAllCategories() {
        log.debug("Fetching all job categories");
        return jobCategoryRepository.findAll()
                .stream()
                .map(cat -> new JobCategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        cat.getCreatedAt()
                ))
                .toList();
    }
}
