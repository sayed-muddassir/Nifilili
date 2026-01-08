package com.nifilili.job.domain;

import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.core.enums.job.JobType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_openings")
@Data
public class JobOpening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    private Long municipalityId;
    private Long wardNumber;
    private String toleName;
    private String postalCode;

    @Column(nullable = false)
    private boolean remote;

    private Double salaryRangeMin;
    private Double salaryRangeMax;

    private Integer numberOfOpenings;

    private LocalDate applicationDeadline;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String skills; // JSON array as String

    private Long viewCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobOpeningStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    private Long createdBy;
    private Long updatedBy;

    @OneToMany(mappedBy = "jobOpening", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobQuestion> questions = new ArrayList<>();

    protected JobOpening() {
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        status = JobOpeningStatus.DRAFT;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // domain methods
    public void open() {
        this.status = JobOpeningStatus.OPEN;
    }

    public void close() {
        this.status = JobOpeningStatus.CLOSED;
    }

    // getters
}
