package com.nifilili.job.domain;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.core.enums.job.JobType;
import com.nifilili.job.events.JobAppliedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JobDomainModelTest {

    // ── JobOpening ──────────────────────────────────────────────────────────

    @Test
    void jobOpening_Builder_ShouldSetAllFields() {
        JobOpening job = JobOpening.builder()
                .businessId(1L)
                .title("Backend Dev")
                .description("Java role")
                .jobType(JobType.FULL_TIME)
                .municipalityId(5L)
                .wardNumber(3L)
                .toleName("Thamel")
                .postalCode("44600")
                .remote(true)
                .salaryRangeMin(50000.0)
                .salaryRangeMax(80000.0)
                .numberOfOpenings(2)
                .skills("[\"Java\"]")
                .viewCount(0L)
                .status(JobOpeningStatus.DRAFT)
                .createdBy(42L)
                .updatedBy(42L)
                .build();

        assertEquals("Backend Dev", job.getTitle());
        assertEquals(JobType.FULL_TIME, job.getJobType());
        assertEquals(JobOpeningStatus.DRAFT, job.getStatus());
        assertTrue(job.isRemote());
        assertEquals(50000.0, job.getSalaryRangeMin());
        assertEquals(42L, job.getCreatedBy());
    }

    @Test
    void jobOpening_Open_ShouldSetStatusToOpen() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.CLOSED).build();

        job.open();

        assertEquals(JobOpeningStatus.OPEN, job.getStatus());
    }

    @Test
    void jobOpening_Close_ShouldSetStatusToClosed() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.OPEN).build();

        job.close();

        assertEquals(JobOpeningStatus.CLOSED, job.getStatus());
    }

    @Test
    void jobOpening_SetStatus_ShouldAllowDirectStatusChange() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.DRAFT).build();

        job.setStatus(JobOpeningStatus.OPEN);

        assertEquals(JobOpeningStatus.OPEN, job.getStatus());
    }

    // ── JobApplication ──────────────────────────────────────────────────────

    @Test
    void jobApplication_Builder_ShouldSetAllFields() {
        JobApplication app = JobApplication.builder()
                .userId(42L)
                .resumeUrl("resume.pdf")
                .coverLetter("My cover letter")
                .status(JobApplicationStatus.RECEIVED)
                .build();

        assertEquals(42L, app.getUserId());
        assertEquals("resume.pdf", app.getResumeUrl());
        assertEquals(JobApplicationStatus.RECEIVED, app.getStatus());
    }

    @Test
    void jobApplication_Withdraw_ShouldSetStatusReasonAndTimestamp() {
        JobApplication app = JobApplication.builder()
                .status(JobApplicationStatus.RECEIVED)
                .build();

        app.withdraw("Changed my mind");

        assertEquals(JobApplicationStatus.WITHDRAWN, app.getStatus());
        assertEquals("Changed my mind", app.getWithdrawalReason());
        assertNotNull(app.getWithdrawnAt());
    }

    @Test
    void jobApplication_ChangeStatus_ShouldUpdateStatus() {
        JobApplication app = JobApplication.builder()
                .status(JobApplicationStatus.RECEIVED)
                .build();

        app.changeStatus(JobApplicationStatus.REVIEWED);

        assertEquals(JobApplicationStatus.REVIEWED, app.getStatus());
    }

    // ── JobCategory ─────────────────────────────────────────────────────────

    @Test
    void jobCategory_Constructor_ShouldSetNameAndCreatedBy() {
        JobCategory category = new JobCategory("Engineering", 1L);

        assertEquals("Engineering", category.getName());
        assertEquals(1L, category.getCreatedBy());
        assertEquals(1L, category.getUpdatedBy());
    }

    // ── JobQuestion ─────────────────────────────────────────────────────────

    @Test
    void jobQuestion_Builder_ShouldSetAllFields() {
        JobQuestion question = JobQuestion.builder()
                .questionText("Why us?")
                .required(true)
                .build();

        assertEquals("Why us?", question.getQuestionText());
        assertTrue(question.isRequired());
    }

    // ── JobApplicationStatusHistory ─────────────────────────────────────────

    @Test
    void jobApplicationStatusHistory_Constructor_ShouldSetAllFields() {
        JobApplicationStatusHistory history = new JobApplicationStatusHistory(
                200L, JobApplicationStatus.RECEIVED, JobApplicationStatus.REVIEWED, "Reviewed", 42L);

        assertEquals(200L, history.getJobApplicationId());
        assertEquals(JobApplicationStatus.RECEIVED, history.getStatusFrom());
        assertEquals(JobApplicationStatus.REVIEWED, history.getStatusTo());
        assertEquals("Reviewed", history.getNotes());
        assertEquals(42L, history.getChangedBy());
        assertNotNull(history.getChangedDate());
    }

    @Test
    void jobApplicationStatusHistory_InitialApplication_ShouldHaveNullStatusFrom() {
        JobApplicationStatusHistory history = new JobApplicationStatusHistory(
                200L, null, JobApplicationStatus.RECEIVED, "Submitted", 42L);

        assertNull(history.getStatusFrom());
        assertEquals(JobApplicationStatus.RECEIVED, history.getStatusTo());
    }

    // ── JobApplicationAnswer ────────────────────────────────────────────────

    @Test
    void jobApplicationAnswer_Constructor_ShouldSetAllFields() {
        JobApplication app = JobApplication.builder().userId(42L).build();
        TestEntityIdUtil.withId(app, 200L);

        JobQuestion question = JobQuestion.builder().questionText("Why?").required(true).build();
        TestEntityIdUtil.withId(question, 300L);

        JobApplicationAnswer answer = new JobApplicationAnswer(app, question, "My answer");

        assertEquals("My answer", answer.getAnswer());
        assertSame(app, answer.getApplication());
        assertSame(question, answer.getQuestion());
    }

    // ── JobAppliedEvent ─────────────────────────────────────────────────────

    @Test
    void jobAppliedEvent_RecordAccessors_ShouldReturnValues() {
        JobAppliedEvent event = new JobAppliedEvent(100L, 200L, 42L);

        assertEquals(100L, event.jobOpeningId());
        assertEquals(200L, event.applicationId());
        assertEquals(42L, event.userId());
    }
}
