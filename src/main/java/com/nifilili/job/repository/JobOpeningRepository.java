package com.nifilili.job.repository;

import com.nifilili.job.domain.JobOpening;
import com.nifilili.core.enums.job.JobOpeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {

    // Public job search
    List<JobOpening> findByStatusAndApplicationDeadlineGreaterThanEqual(
            JobOpeningStatus status,
            LocalDate today
    );

    // Recruiter jobs
    List<JobOpening> findByBusinessId(Long businessId);

    boolean existsByIdAndStatus(Long jobId, JobOpeningStatus status);
}
