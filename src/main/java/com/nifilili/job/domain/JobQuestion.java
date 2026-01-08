package com.nifilili.job.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "job_questions")
@Data
public class JobQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id", nullable = false)
    private JobOpening jobOpening;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private boolean required;

    private Instant createdAt;
    private Instant updatedAt;

    protected JobQuestion() {
    }

    public JobQuestion(JobOpening jobOpening, String questionText, boolean required) {
        this.jobOpening = jobOpening;
        this.questionText = questionText;
        this.required = required;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // getters
}
