package com.nifilili.job.repository;

import com.nifilili.job.domain.JobQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobQuestionRepository extends JpaRepository<JobQuestion, Long> {

    List<JobQuestion> findByJobOpeningId(Long jobOpeningId);
}
