package com.nifilili.job.domain;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.core.enums.job.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "job_openings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOpening extends BaseEntity {

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

    @Column(name = "is_remote", nullable = false)
    private boolean remote;

    private Double salaryRangeMin;
    private Double salaryRangeMax;

    private Integer numberOfOpenings;

    private LocalDate applicationDeadline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Object skills; // JSON array as String

    private Long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobOpeningStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    private Long createdBy;
    private Long updatedBy;

    @OneToMany(mappedBy = "jobOpening", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobQuestion> questions;

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
