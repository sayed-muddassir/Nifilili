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
import com.nifilili.job.dto.request.UpdateJobRequest;
import com.nifilili.job.dto.response.JobApplicationAnswerResponse;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.core.exception.InvalidJobStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.job.repository.*;
import com.nifilili.job.service.RecruiterJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nifilili.core.enums.job.JobApplicationStatus.*;

@Slf4j
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
    @Transactional
    public Long createJob(CreateJobRequest request) {
        log.info("Creating job opening for businessId='{}'", request.businessId());

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
                .municipalityId(request.municipalityId())
                .wardNumber(request.wardNumber())
                .toleName(request.toleName())
                .postalCode(request.postalCode())
                .remote(request.remote())
                .salaryRangeMin(request.salaryRangeMin())
                .salaryRangeMax(request.salaryRangeMax())
                .numberOfOpenings(request.numberOfOpenings())
                .applicationDeadline(request.applicationDeadline())
                .skills(new Gson().toJson(request.skills()))
                .viewCount(0L)
                .createdBy(SecurityUtil.getCurrentUserId())
                .updatedBy(SecurityUtil.getCurrentUserId())
                .build();

        JobOpening opening = jobOpeningRepository.save(jobOpening);

        log.info("Job opening id='{}' created for businessId='{}'", opening.getId(), request.businessId());
        return opening.getId();
    }

    @Override
    @Transactional
    public void updateJob(Long jobId, UpdateJobRequest request) {
        log.info("Updating job opening id='{}'", jobId);

        JobOpening jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + jobId));

        if (jobOpening.getStatus() != JobOpeningStatus.DRAFT) {
            throw new InvalidJobStateException(
                    "Only DRAFT jobs can be updated; current status is " + jobOpening.getStatus());
        }

        jobOpening.setTitle(request.title());
        jobOpening.setDescription(request.description());
        jobOpening.setJobType(request.jobType());
        jobOpening.setMunicipalityId(request.municipalityId());
        jobOpening.setWardNumber(request.wardNumber());
        jobOpening.setToleName(request.toleName());
        jobOpening.setPostalCode(request.postalCode());
        jobOpening.setRemote(request.remote());
        jobOpening.setSalaryRangeMin(request.salaryRangeMin());
        jobOpening.setSalaryRangeMax(request.salaryRangeMax());
        jobOpening.setNumberOfOpenings(request.numberOfOpenings());
        jobOpening.setApplicationDeadline(request.applicationDeadline());
        jobOpening.setSkills(new Gson().toJson(request.skills()));

        jobOpeningRepository.save(jobOpening);

        log.info("Job opening id='{}' updated successfully", jobId);
    }

    @Override
    @Transactional
    public Long createJobQuestion(CreateJobQuestionRequest request, Long jobId) {
        log.info("Adding screening question to jobId='{}'", jobId);

        if (!jobOpeningRepository.existsByIdAndStatus(jobId, JobOpeningStatus.DRAFT)) {
            throw new IllegalArgumentException("Job not found or not in DRAFT status");
        }

        JobQuestion jobQuestion = JobQuestion.builder()
                .jobOpening(jobOpeningRepository.findById(jobId).get())
                .questionText(request.questionText())
                .required(request.required())
                .build();
        JobQuestion savedQuestion = jobQuestionRepository.save(jobQuestion);

        log.info("Screening question id='{}' added to jobId='{}'", savedQuestion.getId(), jobId);
        return savedQuestion.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobQuestionResponse> getJobQuestions(Long jobId) {
        log.debug("Fetching screening questions for jobId='{}'", jobId);
        return jobQuestionRepository.findByJobOpeningId(jobId).stream().map(data ->
                JobQuestionResponse.builder()
                        .id(data.getId())
                        .questionText(data.getQuestionText())
                        .required(data.isRequired())
                        .build()
        ).toList();
    }

    @Override
    @Transactional
    public void publishJob(Long jobId) {
        log.info("Publishing job opening id='{}'", jobId);

        JobOpening jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + jobId));

        if (jobOpening.getStatus() != JobOpeningStatus.DRAFT) {
            throw new InvalidJobStateException(
                    "Only DRAFT jobs can be published; current status is " + jobOpening.getStatus());
        }

        jobOpening.setStatus(JobOpeningStatus.OPEN);
        jobOpeningRepository.save(jobOpening);

        log.info("Job opening id='{}' published successfully", jobId);
    }

    @Override
    @Transactional
    public void closeJob(Long jobId) {
        log.info("Closing job opening id='{}'", jobId);

        JobOpening jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + jobId));

        if (jobOpening.getStatus() != JobOpeningStatus.OPEN) {
            throw new InvalidJobStateException(
                    "Only OPEN jobs can be closed; current status is " + jobOpening.getStatus());
        }

        jobOpening.close();
        jobOpeningRepository.save(jobOpening);

        log.info("Job opening id='{}' closed successfully", jobId);
    }

    @Override
    @Transactional
    public void reopenJob(Long jobId) {
        log.info("Reopening job opening id='{}'", jobId);

        JobOpening jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + jobId));

        if (jobOpening.getStatus() != JobOpeningStatus.CLOSED) {
            throw new InvalidJobStateException(
                    "Only CLOSED jobs can be reopened; current status is " + jobOpening.getStatus());
        }

        jobOpening.open();
        jobOpeningRepository.save(jobOpening);

        log.info("Job opening id='{}' reopened successfully", jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getApplicationsForJob(Long jobOpeningId) {
        log.debug("Fetching applications for jobOpeningId='{}'", jobOpeningId);
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
    @Transactional
    public void changeApplicationStatus(
            Long applicationId,
            ChangeApplicationStatusRequest request
    ) {
        log.info("Changing application id='{}' status to '{}'", applicationId, request.status());

        JobApplication jobApplication = jobApplicationRepository.findById(applicationId).orElseThrow(() ->
                new IllegalArgumentException("Job application not found")
        );

        if (!isJobApplicationStatusAllowed(jobApplication.getStatus(), request.status())) {
            throw new IllegalArgumentException("Invalid status transition from " +
                    jobApplication.getStatus() + " to " + request.status());
        }

        JobApplicationStatus previousStatus = jobApplication.getStatus();

        jobApplicationStatusHistoryRepository.save(new JobApplicationStatusHistory(
                applicationId,
                previousStatus,
                request.status(),
                request.notes(),
                SecurityUtil.getCurrentUserId()
        ));

        jobApplication.changeStatus(request.status());
        jobApplicationRepository.save(jobApplication);

        log.info("Application id='{}' status changed from {} to {}", applicationId, previousStatus, request.status());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationTimelineResponse> getApplicationTimeline(Long applicationId) {
        log.debug("Fetching timeline for applicationId='{}'", applicationId);
        return jobApplicationStatusHistoryRepository.findByJobApplicationIdOrderByChangedDateAsc(applicationId)
                .stream().map(data ->
                        JobApplicationTimelineResponse.builder()
                                .statusFrom(data.getStatusFrom())
                                .statusTo(data.getStatusTo())
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
