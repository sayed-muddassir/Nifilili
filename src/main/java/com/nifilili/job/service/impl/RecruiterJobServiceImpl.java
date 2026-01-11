package com.nifilili.job.service.impl;

import com.google.gson.Gson;
import com.nifilili.business.api.BusinessValidationApi;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.job.domain.JobOpening;
import com.nifilili.job.domain.JobQuestion;
import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.job.repository.JobCategoryRepository;
import com.nifilili.job.repository.JobOpeningRepository;
import com.nifilili.job.repository.JobQuestionRepository;
import com.nifilili.job.service.RecruiterJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterJobServiceImpl implements RecruiterJobService {

    private final BusinessValidationApi businessValidationApi;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final JobQuestionRepository jobQuestionRepository;

    @Override
    public Long createJob(CreateJobRequest request) {
        if (!businessValidationApi.existsAndActive(request.businessId())) {
            throw new IllegalArgumentException("Invalid business ID");
        }

        if (!jobCategoryRepository.existsById(request.jobCategoryId())) {
            throw new IllegalArgumentException("Invalid job category ID");
        }

        JobOpening jobOpening = JobOpening.builder()
                .businessId(request.businessId())
                .category(jobCategoryRepository.findById(request.jobCategoryId()).get())
                .title(request.title())
                .description(request.description())
                .jobType(request.jobType())
                .municipalityId(0L)
                .wardNumber(0L)
                .toleName("TEST DATA")
                .postalCode("TEST DATA")
                .remote(request.remote())
                .salaryRangeMin(request.salaryRangeMin())
                .salaryRangeMax(request.salaryRangeMax())
                .numberOfOpenings(request.numberOfOpenings())
                .applicationDeadline(request.applicationDeadline())
                .skills(new Gson().toJson(request.skills()))
                .viewCount(0L)// TODO: convert list to JSON string
                .createdBy(0L)
                .updatedBy(0L)
                .build();

        JobOpening opening = jobOpeningRepository.save(jobOpening);

        return opening.getId();
    }

    @Override
    public Long createJobQuestion(CreateJobQuestionRequest request, Long jobId) {

        if (!jobOpeningRepository.existsByIdAndStatus(jobId, JobOpeningStatus.DRAFT)) {
            throw new IllegalArgumentException("Job not found or not in DRAFT status");
        }

        JobQuestion jobQuestion = JobQuestion.builder()
                .jobOpening(jobOpeningRepository.findById(jobId).get())
                .questionText(request.questionText())
                .required(request.required())
                .build();
        JobQuestion savedQuestion = jobQuestionRepository.save(jobQuestion);
        return savedQuestion.getId();
    }

    @Override
    public List<JobQuestionResponse> getJobQuestions(Long jobId) {
        return jobQuestionRepository.findByJobOpeningId(jobId).stream().map(data ->
                JobQuestionResponse.builder()
                        .id(data.getId())
                        .questionText(data.getQuestionText())
                        .required(data.isRequired())
                        .build()
        ).toList();
    }

    @Override
    public List<JobApplicationResponse> getApplications(Long jobId) {
        // TODO: fetch applications for job
        return List.of();
    }

    @Override
    public void changeApplicationStatus(
            Long applicationId,
            ChangeApplicationStatusRequest request
    ) {
        // TODO:
        // 1. validate transition
        // 2. update status
        // 3. save history
        // 4. emit event
    }

    @Override
    public void getApplicationTimeline(Long applicationId) {
        // TODO: fetch and return timeline
    }
}
