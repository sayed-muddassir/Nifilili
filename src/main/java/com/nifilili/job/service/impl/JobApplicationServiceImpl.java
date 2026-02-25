package com.nifilili.job.service.impl;

import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.job.domain.*;
import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.events.JobAppliedEvent;
import com.nifilili.job.repository.JobApplicationAnswerRepository;
import com.nifilili.job.repository.JobApplicationRepository;
import com.nifilili.job.repository.JobApplicationStatusHistoryRepository;
import com.nifilili.job.repository.JobOpeningRepository;
import com.nifilili.job.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final ApplicationEventPublisher publisher;
    private final JobOpeningRepository jobOpeningRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationAnswerRepository jobApplicationAnswerRepository;
    private final JobApplicationStatusHistoryRepository jobApplicationStatusHistoryRepository;

    @Override
    @Transactional
    public Long apply(Long jobId, ApplyJobRequest request) {
        log.info("Applying for jobId='{}', userId='{}'", jobId, request.userId());

        if (!jobOpeningRepository.existsByIdAndStatus(jobId, JobOpeningStatus.OPEN)) {
            throw new IllegalArgumentException("Job is not open for applications");
        }
        if (jobApplicationRepository.existsByJobOpeningIdAndUserId(jobId, request.userId())) {
            throw new IllegalArgumentException("User has already applied for this job");
        }

        JobOpening jobOpening = jobOpeningRepository.findById(jobId).get();

        List<JobQuestion> jobOpeningQuestions = jobOpening.getQuestions();

        Map<Long, String> answers = request.answers();

        JobApplication jobApplication = JobApplication.builder()
                .jobOpening(jobOpening)
                .userId(request.userId())
                .resumeUrl(request.resumeUrl())
                .coverLetter(request.coverLetter())
                .status(JobApplicationStatus.RECEIVED)
                .withdrawalReason("NA")
                .withdrawnAt(Instant.now())
                .build();

        JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        List<JobApplicationAnswer> jobApplicationAnswers = jobOpeningQuestions.stream().map(jobQuestion -> {
            if (answers.containsKey(jobQuestion.getId())) {
                return jobApplicationAnswerRepository.save(
                        new JobApplicationAnswer(savedJobApplication, jobQuestion, answers.get(jobQuestion.getId())));
            } else {
                throw new IllegalArgumentException("Missing answer for question id: " + jobQuestion.getId());
            }
        }).toList();

        jobApplication.setAnswers(jobApplicationAnswers);

        // Initial application: no previous status (statusFrom is null)
        jobApplicationStatusHistoryRepository.save(new JobApplicationStatusHistory(
                jobApplication.getId(),
                null,
                JobApplicationStatus.RECEIVED,
                "Application submitted",
                SecurityUtil.getCurrentUserId()
        ));

        publisher.publishEvent(new JobAppliedEvent(jobId, savedJobApplication.getId(), request.userId()));

        log.info("Application id='{}' submitted for jobId='{}'", savedJobApplication.getId(), jobId);
        return savedJobApplication.getId();
    }

    @Override
    @Transactional
    public void withdraw(Long applicationId, String reason) {
        log.info("Withdrawing application id='{}'", applicationId);

        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Job application not found"));

        // Requirement: users can withdraw only BEFORE the business moves status to REVIEWED
        if (jobApplication.getStatus() != JobApplicationStatus.RECEIVED) {
            throw new IllegalStateException(
                    "Withdrawal is only allowed before the application is reviewed; current status is "
                            + jobApplication.getStatus());
        }

        JobApplicationStatus previousStatus = jobApplication.getStatus();

        jobApplicationStatusHistoryRepository.save(new JobApplicationStatusHistory(
                jobApplication.getId(),
                previousStatus,
                JobApplicationStatus.WITHDRAWN,
                reason,
                SecurityUtil.getCurrentUserId()
        ));

        jobApplication.withdraw(reason);
        jobApplication.changeStatus(JobApplicationStatus.WITHDRAWN);
        jobApplicationRepository.save(jobApplication);

        log.info("Application id='{}' withdrawn successfully", applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyApplicationResponse> getMyApplications(Long userId) {
        log.debug("Fetching applications for userId='{}'", userId);
        return jobApplicationRepository.findByUserId(userId).stream().map(jobApplication -> MyApplicationResponse.builder()
                .applicationId(jobApplication.getId())
                .jobTitle(jobApplication.getJobOpening().getTitle())
                .jobDescription(jobApplication.getJobOpening().getDescription())
                .applicationStatus(jobApplication.getStatus())
                .build()).toList();
    }
}
