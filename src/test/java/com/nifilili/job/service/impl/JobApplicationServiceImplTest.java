package com.nifilili.job.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.job.domain.JobApplication;
import com.nifilili.job.domain.JobApplicationAnswer;
import com.nifilili.job.domain.JobApplicationStatusHistory;
import com.nifilili.job.domain.JobOpening;
import com.nifilili.job.domain.JobQuestion;
import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.events.JobAppliedEvent;
import com.nifilili.job.repository.JobApplicationAnswerRepository;
import com.nifilili.job.repository.JobApplicationRepository;
import com.nifilili.job.repository.JobApplicationStatusHistoryRepository;
import com.nifilili.job.repository.JobOpeningRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long JOB_ID = 100L;
    private static final Long APPLICATION_ID = 200L;
    private static final Long QUESTION_ID = 300L;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private JobOpeningRepository jobOpeningRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobApplicationAnswerRepository jobApplicationAnswerRepository;

    @Mock
    private JobApplicationStatusHistoryRepository jobApplicationStatusHistoryRepository;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ── apply ───────────────────────────────────────────────────────────────

    @Test
    void apply_WhenJobIsOpenAndNotDuplicate_ShouldCreateApplicationAndPublishEvent() {
        JobQuestion question = JobQuestion.builder().questionText("Why us?").required(true).build();
        TestEntityIdUtil.withId(question, QUESTION_ID);

        JobOpening jobOpening = JobOpening.builder()
                .status(JobOpeningStatus.OPEN)
                .title("Dev")
                .questions(List.of(question))
                .build();
        TestEntityIdUtil.withId(jobOpening, JOB_ID);

        when(jobOpeningRepository.existsByIdAndStatus(JOB_ID, JobOpeningStatus.OPEN)).thenReturn(true);
        when(jobApplicationRepository.existsByJobOpeningIdAndUserId(JOB_ID, USER_ID)).thenReturn(false);
        when(jobOpeningRepository.findById(JOB_ID)).thenReturn(Optional.of(jobOpening));

        JobApplication savedApp = JobApplication.builder()
                .jobOpening(jobOpening)
                .userId(USER_ID)
                .status(JobApplicationStatus.RECEIVED)
                .build();
        TestEntityIdUtil.withId(savedApp, APPLICATION_ID);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(savedApp);

        JobApplicationAnswer savedAnswer = new JobApplicationAnswer(savedApp, question, "Passion");
        when(jobApplicationAnswerRepository.save(any(JobApplicationAnswer.class))).thenReturn(savedAnswer);

        ApplyJobRequest request = new ApplyJobRequest(
                USER_ID, "https://resume.pdf", "Cover letter", Map.of(QUESTION_ID, "Passion"));

        Long result = jobApplicationService.apply(JOB_ID, request);

        assertEquals(APPLICATION_ID, result);

        // Verify history saved with null statusFrom
        ArgumentCaptor<JobApplicationStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(JobApplicationStatusHistory.class);
        verify(jobApplicationStatusHistoryRepository).save(historyCaptor.capture());
        assertNull(historyCaptor.getValue().getStatusFrom());
        assertEquals(JobApplicationStatus.RECEIVED, historyCaptor.getValue().getStatusTo());

        // Verify event published
        ArgumentCaptor<JobAppliedEvent> eventCaptor = ArgumentCaptor.forClass(JobAppliedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertEquals(JOB_ID, eventCaptor.getValue().jobOpeningId());
        assertEquals(APPLICATION_ID, eventCaptor.getValue().applicationId());
        assertEquals(USER_ID, eventCaptor.getValue().userId());
    }

    @Test
    void apply_WhenJobIsNotOpen_ShouldThrowIllegalArgument() {
        when(jobOpeningRepository.existsByIdAndStatus(JOB_ID, JobOpeningStatus.OPEN)).thenReturn(false);

        ApplyJobRequest request = new ApplyJobRequest(USER_ID, "url", "cl", Map.of());

        assertThrows(IllegalArgumentException.class, () -> jobApplicationService.apply(JOB_ID, request));
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void apply_WhenUserAlreadyApplied_ShouldThrowIllegalArgument() {
        when(jobOpeningRepository.existsByIdAndStatus(JOB_ID, JobOpeningStatus.OPEN)).thenReturn(true);
        when(jobApplicationRepository.existsByJobOpeningIdAndUserId(JOB_ID, USER_ID)).thenReturn(true);

        ApplyJobRequest request = new ApplyJobRequest(USER_ID, "url", "cl", Map.of());

        assertThrows(IllegalArgumentException.class, () -> jobApplicationService.apply(JOB_ID, request));
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void apply_WhenMissingAnswer_ShouldThrowIllegalArgument() {
        JobQuestion question = JobQuestion.builder().questionText("Why?").required(true).build();
        TestEntityIdUtil.withId(question, QUESTION_ID);

        JobOpening jobOpening = JobOpening.builder()
                .status(JobOpeningStatus.OPEN)
                .questions(List.of(question))
                .build();
        TestEntityIdUtil.withId(jobOpening, JOB_ID);

        when(jobOpeningRepository.existsByIdAndStatus(JOB_ID, JobOpeningStatus.OPEN)).thenReturn(true);
        when(jobApplicationRepository.existsByJobOpeningIdAndUserId(JOB_ID, USER_ID)).thenReturn(false);
        when(jobOpeningRepository.findById(JOB_ID)).thenReturn(Optional.of(jobOpening));

        JobApplication savedApp = JobApplication.builder().jobOpening(jobOpening).userId(USER_ID).build();
        TestEntityIdUtil.withId(savedApp, APPLICATION_ID);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(savedApp);

        // Empty answers map — missing required answer
        ApplyJobRequest request = new ApplyJobRequest(USER_ID, "url", "cl", Map.of());

        assertThrows(IllegalArgumentException.class, () -> jobApplicationService.apply(JOB_ID, request));
    }

    // ── withdraw ────────────────────────────────────────────────────────────

    @Test
    void withdraw_WhenStatusIsReceived_ShouldWithdrawAndSaveHistory() {
        JobApplication application = JobApplication.builder()
                .status(JobApplicationStatus.RECEIVED)
                .build();
        TestEntityIdUtil.withId(application, APPLICATION_ID);
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        jobApplicationService.withdraw(APPLICATION_ID, "Changed my mind");

        // Verify history
        ArgumentCaptor<JobApplicationStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(JobApplicationStatusHistory.class);
        verify(jobApplicationStatusHistoryRepository).save(historyCaptor.capture());
        assertEquals(JobApplicationStatus.RECEIVED, historyCaptor.getValue().getStatusFrom());
        assertEquals(JobApplicationStatus.WITHDRAWN, historyCaptor.getValue().getStatusTo());
        assertEquals("Changed my mind", historyCaptor.getValue().getNotes());

        // Verify application saved
        ArgumentCaptor<JobApplication> appCaptor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(appCaptor.capture());
        assertEquals(JobApplicationStatus.WITHDRAWN, appCaptor.getValue().getStatus());
    }

    @Test
    void withdraw_WhenApplicationNotFound_ShouldThrowIllegalArgument() {
        when(jobApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> jobApplicationService.withdraw(999L, "reason"));
    }

    @Test
    void withdraw_WhenStatusIsReviewed_ShouldThrowIllegalState() {
        JobApplication application = JobApplication.builder()
                .status(JobApplicationStatus.REVIEWED)
                .build();
        TestEntityIdUtil.withId(application, APPLICATION_ID);
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(IllegalStateException.class,
                () -> jobApplicationService.withdraw(APPLICATION_ID, "Too late"));
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void withdraw_WhenStatusIsShortlisted_ShouldThrowIllegalState() {
        JobApplication application = JobApplication.builder()
                .status(JobApplicationStatus.SHORTLISTED)
                .build();
        TestEntityIdUtil.withId(application, APPLICATION_ID);
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(IllegalStateException.class,
                () -> jobApplicationService.withdraw(APPLICATION_ID, "reason"));
    }

    // ── getMyApplications ───────────────────────────────────────────────────

    @Test
    void getMyApplications_WhenUserHasApplications_ShouldReturnMappedList() {
        JobOpening jobOpening = JobOpening.builder().title("Backend Dev").description("Java role").build();
        TestEntityIdUtil.withId(jobOpening, JOB_ID);

        JobApplication app = JobApplication.builder()
                .jobOpening(jobOpening)
                .userId(USER_ID)
                .status(JobApplicationStatus.RECEIVED)
                .build();
        TestEntityIdUtil.withId(app, APPLICATION_ID);

        when(jobApplicationRepository.findByUserId(USER_ID)).thenReturn(List.of(app));

        List<MyApplicationResponse> result = jobApplicationService.getMyApplications(USER_ID);

        assertEquals(1, result.size());
        assertEquals(APPLICATION_ID, result.get(0).getApplicationId());
        assertEquals("Backend Dev", result.get(0).getJobTitle());
        assertEquals(JobApplicationStatus.RECEIVED, result.get(0).getApplicationStatus());
    }

    @Test
    void getMyApplications_WhenNoApplications_ShouldReturnEmptyList() {
        when(jobApplicationRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<MyApplicationResponse> result = jobApplicationService.getMyApplications(USER_ID);

        assertTrue(result.isEmpty());
    }
}
