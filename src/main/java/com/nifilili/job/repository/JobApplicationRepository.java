package com.nifilili.job.repository;

import com.nifilili.job.domain.JobApplication;
import com.nifilili.core.enums.job.JobApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // Prevent duplicate applications
    boolean existsByJobOpeningIdAndUserId(Long jobOpeningId, Long userId);

    // Applicant dashboard
    List<JobApplication> findByUserId(Long userId);

    // Recruiter pipeline
    List<JobApplication> findByJobOpeningId(Long jobOpeningId);

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);

    List<JobApplication> findByJobOpeningIdAndStatus(
            Long jobOpeningId,
            JobApplicationStatus status
    );
}
