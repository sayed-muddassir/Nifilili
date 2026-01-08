package com.nifilili.job.domain;

import com.nifilili.core.enums.job.JobApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;

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
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    protected JobApplication() {
    }

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
