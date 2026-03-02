package com.nifilili.job.repository;

import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.job.domain.JobApplication;
import com.nifilili.job.domain.JobApplicationAnswer;
import com.nifilili.job.domain.JobApplicationStatusHistory;
import com.nifilili.job.domain.JobCategory;
import com.nifilili.job.domain.JobOpening;
import com.nifilili.job.domain.JobQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests verifying Job repository interfaces extend JpaRepository
 * and declare the expected custom query methods.
 */
class JobRepositoryContractTest {

    // ── JobOpeningRepository ────────────────────────────────────────────────

    @Test
    void jobOpeningRepository_ShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(JobOpeningRepository.class));
    }

    @Test
    void jobOpeningRepository_ShouldDeclareSearchOpenJobs() throws NoSuchMethodException {
        Method method = JobOpeningRepository.class.getMethod("searchOpenJobs", String.class, Long.class);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void jobOpeningRepository_ShouldDeclareExistsByIdAndStatus() throws NoSuchMethodException {
        Method method = JobOpeningRepository.class.getMethod("existsByIdAndStatus", Long.class, JobOpeningStatus.class);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    void jobOpeningRepository_ShouldDeclareFindByBusinessId() throws NoSuchMethodException {
        Method method = JobOpeningRepository.class.getMethod("findByBusinessId", Long.class);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void jobOpeningRepository_ShouldDeclareFindByStatusAndDeadline() throws NoSuchMethodException {
        Method method = JobOpeningRepository.class.getMethod(
                "findByStatusAndApplicationDeadlineGreaterThanEqual",
                JobOpeningStatus.class, LocalDate.class);
        assertEquals(List.class, method.getReturnType());
    }

    // ── JobCategoryRepository ───────────────────────────────────────────────

    @Test
    void jobCategoryRepository_ShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(JobCategoryRepository.class));
    }

    @Test
    void jobCategoryRepository_ShouldDeclareExistsByNameIgnoreCase() throws NoSuchMethodException {
        Method method = JobCategoryRepository.class.getMethod("existsByNameIgnoreCase", String.class);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    void jobCategoryRepository_ShouldDeclareFindByNameIgnoreCase() throws NoSuchMethodException {
        Method method = JobCategoryRepository.class.getMethod("findByNameIgnoreCase", String.class);
        assertEquals(Optional.class, method.getReturnType());
    }

    // ── JobApplicationRepository ────────────────────────────────────────────

    @Test
    void jobApplicationRepository_ShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(JobApplicationRepository.class));
    }

    @Test
    void jobApplicationRepository_ShouldDeclareExistsByJobOpeningIdAndUserId() throws NoSuchMethodException {
        Method method = JobApplicationRepository.class.getMethod(
                "existsByJobOpeningIdAndUserId", Long.class, Long.class);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    void jobApplicationRepository_ShouldDeclareFindByUserId() throws NoSuchMethodException {
        Method method = JobApplicationRepository.class.getMethod("findByUserId", Long.class);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void jobApplicationRepository_ShouldDeclareFindByJobOpeningId() throws NoSuchMethodException {
        Method method = JobApplicationRepository.class.getMethod("findByJobOpeningId", Long.class);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void jobApplicationRepository_ShouldDeclareFindByIdAndUserId() throws NoSuchMethodException {
        Method method = JobApplicationRepository.class.getMethod("findByIdAndUserId", Long.class, Long.class);
        assertEquals(Optional.class, method.getReturnType());
    }

    @Test
    void jobApplicationRepository_ShouldDeclareFindByJobOpeningIdAndStatus() throws NoSuchMethodException {
        Method method = JobApplicationRepository.class.getMethod(
                "findByJobOpeningIdAndStatus", Long.class, JobApplicationStatus.class);
        assertEquals(List.class, method.getReturnType());
    }

    // ── JobQuestionRepository ───────────────────────────────────────────────

    @Test
    void jobQuestionRepository_ShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(JobQuestionRepository.class));
    }

    @Test
    void jobQuestionRepository_ShouldDeclareFindByJobOpeningId() throws NoSuchMethodException {
        Method method = JobQuestionRepository.class.getMethod("findByJobOpeningId", Long.class);
        assertEquals(List.class, method.getReturnType());
    }

    // ── JobApplicationAnswerRepository ──────────────────────────────────────

    @Test
    void jobApplicationAnswerRepository_ShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(JobApplicationAnswerRepository.class));
    }

    @Test
    void jobApplicationAnswerRepository_ShouldDeclareFindByApplicationId() throws NoSuchMethodException {
        Method method = JobApplicationAnswerRepository.class.getMethod("findByApplicationId", Long.class);
        assertEquals(List.class, method.getReturnType());
    }

    // ── JobApplicationStatusHistoryRepository ───────────────────────────────

    @Test
    void jobApplicationStatusHistoryRepository_ShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(JobApplicationStatusHistoryRepository.class));
    }

    @Test
    void jobApplicationStatusHistoryRepository_ShouldDeclareFindByApplicationIdOrderByDate()
            throws NoSuchMethodException {
        Method method = JobApplicationStatusHistoryRepository.class.getMethod(
                "findByJobApplicationIdOrderByChangedDateAsc", Long.class);
        assertEquals(List.class, method.getReturnType());
    }
}
