package com.nifilili.job.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.job.JobApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "job_application_status_history")
@Data
public class JobApplicationStatusHistory extends BaseEntity {

    private Long jobApplicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Long changedBy;

    @Column(nullable = false)
    private Instant changedDate;

    protected JobApplicationStatusHistory() {
    }

    public JobApplicationStatusHistory(
            Long jobApplicationId,
            JobApplicationStatus status,
            String notes,
            Long changedBy
    ) {
        this.jobApplicationId = jobApplicationId;
        this.status = status;
        this.notes = notes;
        this.changedBy = changedBy;
        this.changedDate = Instant.now();
    }

    // getters
}
