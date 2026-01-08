package com.nifilili.job.repository;

import com.nifilili.job.domain.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    Optional<JobCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
