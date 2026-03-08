package com.nifilili.job.repository;

import com.nifilili.job.domain.JobOpening;
import com.nifilili.core.enums.job.JobOpeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {

    // Public job search
    List<JobOpening> findByStatusAndApplicationDeadlineGreaterThanEqual(
            JobOpeningStatus status,
            LocalDate today
    );

    /**
     * Searches open job openings filtered by optional keyword (title/description) and categoryId.
     */
    @Query("SELECT j FROM JobOpening j WHERE j.status = 'OPEN'"
            + " AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))"
            + "      OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))"
            + " AND (:categoryId IS NULL OR j.category.id = :categoryId)"
            + " ORDER BY j.createdAt DESC")
    List<JobOpening> searchOpenJobs(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    // Recruiter jobs
    List<JobOpening> findByBusinessId(Long businessId);

    boolean existsByIdAndStatus(Long jobId, JobOpeningStatus status);
}
