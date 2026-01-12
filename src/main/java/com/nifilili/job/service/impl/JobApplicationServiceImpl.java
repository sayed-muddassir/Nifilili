package com.nifilili.job.service.impl;

import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl implements JobApplicationService {

    private final ApplicationEventPublisher publisher;
    private final JobOpeningRepository jobOpeningRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationAnswerRepository jobApplicationAnswerRepository;
    private final JobApplicationStatusHistoryRepository jobApplicationStatusHistoryRepository;

    public Long apply(Long jobId, ApplyJobRequest request) {
        // TODO:
        // 1. validate job is open
        // 2. validate user not already applied
        // 3. create JobApplication
        // 4. save answers
        // 5. create status history
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
                .withdrawalReason("Not applicable")
                .withdrawnAt(Instant.now())
                .build();

        JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        List<JobApplicationAnswer> jobApplicationAnswers = jobOpeningQuestions.stream().map(jobQuestion -> {
            if (answers.containsKey(jobQuestion.getId())) {
                return jobApplicationAnswerRepository.save(new JobApplicationAnswer(savedJobApplication, jobQuestion, answers.get(jobQuestion.getId())));
            } else {
                throw new IllegalArgumentException("Missing answer for question id: " + jobQuestion.getId());
            }
        }).toList();

        jobApplication.setAnswers(jobApplicationAnswers);

        jobApplicationStatusHistoryRepository.save(new JobApplicationStatusHistory(
                jobApplication.getId(),
                JobApplicationStatus.RECEIVED,
                "Application submitted",
                0L// system user ID

        ));

        publisher.publishEvent(new JobAppliedEvent(jobId, savedJobApplication.getId(), request.userId()));

        return savedJobApplication.getId();
    }

    public void withdraw(Long applicationId, String reason) {
        // TODO:
        // 1. load application
        // 2. ensure status == REVIEWED
        // 3. mark withdrawn
        // 4. save status history

        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Job application not found"));

        if (!jobApplication.getStatus().equals(JobApplicationStatus.REVIEWED)) {
            throw new IllegalStateException("Only applications with status REVIEWED can be withdrawn");
        }

        jobApplicationStatusHistoryRepository.save(new JobApplicationStatusHistory(jobApplication.getId(),
                JobApplicationStatus.WITHDRAWN, reason, 0L));

        jobApplication.withdraw(reason);
        jobApplication.changeStatus(JobApplicationStatus.WITHDRAWN);
        jobApplicationRepository.save(jobApplication);

        // TODO: emit event when status is changed
    }

    public List<MyApplicationResponse> getMyApplications(Long userId) {
        // TODO: fetch by logged-in user
        return jobApplicationRepository.findByUserId(userId).stream().map(jobApplication -> MyApplicationResponse.builder()
                .applicationId(jobApplication.getId())
                .jobTitle(jobApplication.getJobOpening().getTitle())
                .jobDescription(jobApplication.getJobOpening().getDescription())
                .applicationStatus(jobApplication.getStatus())
                .build()).toList();
    }
}
