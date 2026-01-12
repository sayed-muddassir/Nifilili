package com.nifilili.job.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.job.JobApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"job_opening_id", "user_id"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id", nullable = false)
    private JobOpening jobOpening;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobApplicationStatus status;

    private String withdrawalReason;
    private Instant withdrawnAt;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobApplicationAnswer> answers = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        status = JobApplicationStatus.RECEIVED;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void withdraw(String reason) {
        this.status = JobApplicationStatus.WITHDRAWN;
        this.withdrawalReason = reason;
        this.withdrawnAt = Instant.now();
    }

    public void changeStatus(JobApplicationStatus newStatus) {
        this.status = newStatus;
    }

    // getters
}
