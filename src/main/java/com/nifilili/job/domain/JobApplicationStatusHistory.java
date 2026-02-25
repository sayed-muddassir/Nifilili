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
    @Column(name = "status_from")
    private JobApplicationStatus statusFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_to", nullable = false)
    private JobApplicationStatus statusTo;

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
            JobApplicationStatus statusFrom,
            JobApplicationStatus statusTo,
            String notes,
            Long changedBy
    ) {
        this.jobApplicationId = jobApplicationId;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
        this.notes = notes;
        this.changedBy = changedBy;
        this.changedDate = Instant.now();
    }

    // getters
}
