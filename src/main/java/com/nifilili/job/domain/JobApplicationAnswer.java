package com.nifilili.job.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "job_application_answers")
@Data
public class JobApplicationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_application_id", nullable = false)
    private JobApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_question_id", nullable = false)
    private JobQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    private Instant createdAt;

    protected JobApplicationAnswer() {
    }

    public JobApplicationAnswer(
            JobApplication application,
            JobQuestion question,
            String answer
    ) {
        this.application = application;
        this.question = question;
        this.answer = answer;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    // getters
}
