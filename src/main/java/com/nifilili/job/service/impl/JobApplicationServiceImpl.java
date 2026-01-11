package com.nifilili.job.service.impl;

import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.job.domain.JobApplication;
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

import java.util.List;

@Service
@RequiredArgsConstructor
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
            ;
            throw new IllegalArgumentException("User has already applied for this job");
        }

        JobApplication application = JobApplication.builder()
                .jobOpening(jobOpeningRepository.findById(jobId).get())
                .userId(request.userId())
                .coverLetter(request.coverLetter())
                .status(JobApplicationStatus.RECEIVED)
                .build();

        Long applicationId = jobApplicationRepository.save(application).getId();

        publisher.publishEvent(new JobAppliedEvent(jobId, applicationId, request.userId()));

        return applicationId;
    }

    public void withdraw(Long applicationId, String reason) {
        // TODO:
        // 1. load application
        // 2. ensure status == RECEIVED
        // 3. mark withdrawn
        // 4. save status history
    }

    public List<MyApplicationResponse> getMyApplications(Long userId) {
        // TODO: fetch by logged-in user
        return List.of();
    }
}
