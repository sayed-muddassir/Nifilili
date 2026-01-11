package com.nifilili.job.service.impl;

import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;
import com.nifilili.job.repository.JobOpeningRepository;
import com.nifilili.job.service.JobQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobQueryServiceImpl implements JobQueryService {

    private final JobOpeningRepository jobOpeningRepository;

    public List<JobSummaryResponse> searchJobs(String keyword, Long categoryId) {
        return jobOpeningRepository.findAll().stream().map(data -> {
            JobSummaryResponse response = new JobSummaryResponse();
            response.setTitle(data.getTitle());
            response.setDescription(data.getDescription());
            response.setJobType(data.getJobType());
            return response;
        }).toList();
    }

    public JobDetailsResponse getJobDetails(Long jobId) {
        return jobOpeningRepository.findById(jobId).map(data -> {
            JobDetailsResponse response = new JobDetailsResponse();
            response.setTitle(data.getTitle());
            response.setDescription(data.getDescription());
            response.setJobType(data.getJobType());
            response.setMunicipalityId(data.getMunicipalityId());
            response.setWardNumber(data.getWardNumber());
            response.setToleName(data.getToleName());
            response.setPostalCode(data.getPostalCode());
            response.setRemote(data.isRemote());
            response.setSalaryRangeMin(data.getSalaryRangeMin());
            response.setSalaryRangeMax(data.getSalaryRangeMax());
            response.setNumberOfOpenings(data.getNumberOfOpenings());
            response.setApplicationDeadline(data.getApplicationDeadline());
            return response;
    }).get();
    }
}
