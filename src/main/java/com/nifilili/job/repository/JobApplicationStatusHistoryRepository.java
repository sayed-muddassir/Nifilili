package com.nifilili.job.repository;

import com.nifilili.job.domain.JobApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationStatusHistoryRepository
        extends JpaRepository<JobApplicationStatusHistory, Long> {

    List<JobApplicationStatusHistory> findByJobApplicationIdOrderByChangedDateAsc(
            Long jobApplicationId
    );
}
