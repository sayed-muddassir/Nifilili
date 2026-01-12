package com.nifilili.job.service.impl;

import com.google.gson.Gson;
import com.nifilili.business.api.BusinessValidationApi;
import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.job.domain.JobApplication;
import com.nifilili.job.domain.JobApplicationStatusHistory;
import com.nifilili.job.domain.JobOpening;
import com.nifilili.job.domain.JobQuestion;
import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.response.JobApplicationAnswerResponse;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.job.repository.*;
import com.nifilili.job.service.RecruiterJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.nifilili.core.enums.job.JobApplicationStatus.*;

@Service
@RequiredArgsConstructor
public class RecruiterJobServiceImpl implements RecruiterJobService {

    private final BusinessValidationApi businessValidationApi;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final JobQuestionRepository jobQuestionRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationAnswerRepository jobApplicationAnswerRepository;
    private final JobApplicationStatusHistoryRepository jobApplicationStatusHistoryRepository;

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
    public void publishJob(Long jobId) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobId).get();
        jobOpening.setStatus(JobOpeningStatus.OPEN);
        jobOpeningRepository.save(jobOpening);
    }

    @Override
    public List<JobApplicationResponse> getApplicationsForJob(Long jobOpeningId) {
        // fetch applications for job by jobOpeningId
        return jobApplicationRepository.findByJobOpeningId(jobOpeningId)
                .stream().map(jobApplication -> JobApplicationResponse
                        .builder()
                        .jobApplicationId(jobApplication.getId())
                        .jobOpeningId(jobApplication.getJobOpening().getId())
                        .resumeUrl(jobApplication.getResumeUrl())
                        .coverLetter(jobApplication.getCoverLetter())
                        .userId(jobApplication.getUserId())
                        .status(jobApplication.getStatus())
                        .answers(jobApplicationAnswerRepository.findByApplicationId(jobApplication.getId())
                                .stream()
                                .map(answer -> JobApplicationAnswerResponse.builder()
                                        .questionId(answer.getQuestion().getId())
                                        .questionText(answer.getQuestion().getQuestionText())
                                        .answer(answer.getAnswer())
                                        .build())
                                .toList())
                        .build()
                )
                .toList();
    }

    @Override
    public void changeApplicationStatus(
            Long applicationId,
            ChangeApplicationStatusRequest request
    ) {
        // 1. validate transition
        // 2. update status
        // 3. save history
        // 4. emit event

        JobApplication jobApplication = jobApplicationRepository.findById(applicationId).orElseThrow(() ->
                new IllegalArgumentException("Job application not found")
        );

        if (!isJobApplicationStatusAllowed(jobApplication.getStatus(), request.status())) {
            throw new IllegalArgumentException("Invalid status transition from " +
                    jobApplication.getStatus() + " to " + request.status());
        }

        jobApplicationStatusHistoryRepository.save(new JobApplicationStatusHistory(
                applicationId,
                request.status(),
                request.notes(),
                0L // system user ID
        ));

        // TODO: emit event to update the application status in JobApplication table
        jobApplication.changeStatus(request.status());
        jobApplicationRepository.save(jobApplication);
    }

    @Override
    public List<JobApplicationTimelineResponse> getApplicationTimeline(Long applicationId) {
        // fetch and return timeline
        return jobApplicationStatusHistoryRepository.findByJobApplicationIdOrderByChangedDateAsc(applicationId)
                .stream().map(data ->
                        JobApplicationTimelineResponse.builder()
                                .status(data.getStatus())
                                .changedDate(data.getChangedDate())
                                .changedBy(data.getChangedBy())
                                .notes(data.getNotes())
                                .build()
                ).toList();
    }

    // Helper method to validate status transitions

    public static boolean isJobApplicationStatusAllowed(JobApplicationStatus from, JobApplicationStatus to) {
        return switch (from) {
            case RECEIVED -> to == REVIEWED || to == REJECTED;
            case REVIEWED -> to == SHORTLISTED || to == REJECTED;
            case SHORTLISTED -> to == INTERVIEWING;
            case INTERVIEWING -> to == HIRED || to == REJECTED;
            default -> false;
        };
    }
}
