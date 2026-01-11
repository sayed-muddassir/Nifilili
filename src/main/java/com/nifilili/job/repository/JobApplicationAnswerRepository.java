package com.nifilili.job.repository;

import com.nifilili.job.domain.JobApplicationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationAnswerRepository
        extends JpaRepository<JobApplicationAnswer, Long> {

    List<JobApplicationAnswer> findByApplicationId(Long applicationId);
}
