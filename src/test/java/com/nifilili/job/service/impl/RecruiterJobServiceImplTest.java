package com.nifilili.job.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.business.api.BusinessValidationApi;
import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.core.enums.job.JobType;
import com.nifilili.core.exception.InvalidJobStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.job.domain.JobApplication;
import com.nifilili.job.domain.JobApplicationAnswer;
import com.nifilili.job.domain.JobApplicationStatusHistory;
import com.nifilili.job.domain.JobCategory;
import com.nifilili.job.domain.JobOpening;
import com.nifilili.job.domain.JobQuestion;
import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.request.UpdateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.job.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruiterJobServiceImplTest {

    @Mock
    private BusinessValidationApi businessValidationApi;

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @Mock
    private JobOpeningRepository jobOpeningRepository;

    @Mock
    private JobQuestionRepository jobQuestionRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobApplicationAnswerRepository jobApplicationAnswerRepository;

    @Mock
    private JobApplicationStatusHistoryRepository jobApplicationStatusHistoryRepository;

    @InjectMocks
    private RecruiterJobServiceImpl recruiterJobService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(42L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // ── createJob ───────────────────────────────────────────────────────────

    private CreateJobRequest buildCreateRequest() {
        return new CreateJobRequest(
                1L, 10L, "Backend Dev", "Java developer role", JobType.FULL_TIME,
                5L, 3L, "Tole1", "44600", false,
                50000.0, 80000.0, 2, LocalDate.now().plusDays(30),
                List.of("Java", "Spring"));
    }

    @Test
    void createJob_WhenValidRequest_ShouldSaveAndReturnId() {
        CreateJobRequest request = buildCreateRequest();

        when(businessValidationApi.existsAndActive(1L)).thenReturn(true);
        when(jobCategoryRepository.existsById(10L)).thenReturn(true);

        JobCategory category = new JobCategory("Tech", 1L);
        TestEntityIdUtil.withId(category, 10L);
        when(jobCategoryRepository.findById(10L)).thenReturn(Optional.of(category));

        JobOpening saved = JobOpening.builder().title("Backend Dev").build();
        TestEntityIdUtil.withId(saved, 500L);
        when(jobOpeningRepository.save(any(JobOpening.class))).thenReturn(saved);

        Long result = recruiterJobService.createJob(request);

        assertEquals(500L, result);
        ArgumentCaptor<JobOpening> captor = ArgumentCaptor.forClass(JobOpening.class);
        verify(jobOpeningRepository).save(captor.capture());
        assertEquals("Backend Dev", captor.getValue().getTitle());
        assertEquals(1L, captor.getValue().getBusinessId());
    }

    @Test
    void createJob_WhenInvalidBusinessId_ShouldThrowIllegalArgument() {
        when(businessValidationApi.existsAndActive(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> recruiterJobService.createJob(buildCreateRequest()));
        verify(jobOpeningRepository, never()).save(any());
    }

    @Test
    void createJob_WhenInvalidCategoryId_ShouldThrowIllegalArgument() {
        when(businessValidationApi.existsAndActive(1L)).thenReturn(true);
        when(jobCategoryRepository.existsById(10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> recruiterJobService.createJob(buildCreateRequest()));
        verify(jobOpeningRepository, never()).save(any());
    }

    // ── createJobQuestion ───────────────────────────────────────────────────

    @Test
    void createJobQuestion_WhenDraftJob_ShouldSaveAndReturnId() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.DRAFT).build();
        TestEntityIdUtil.withId(job, 100L);

        when(jobOpeningRepository.existsByIdAndStatus(100L, JobOpeningStatus.DRAFT)).thenReturn(true);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        JobQuestion savedQuestion = JobQuestion.builder().questionText("Why us?").required(true).build();
        TestEntityIdUtil.withId(savedQuestion, 600L);
        when(jobQuestionRepository.save(any(JobQuestion.class))).thenReturn(savedQuestion);

        CreateJobQuestionRequest request = new CreateJobQuestionRequest("Why us?", true);
        Long result = recruiterJobService.createJobQuestion(request, 100L);

        assertEquals(600L, result);
        ArgumentCaptor<JobQuestion> captor = ArgumentCaptor.forClass(JobQuestion.class);
        verify(jobQuestionRepository).save(captor.capture());
        assertEquals("Why us?", captor.getValue().getQuestionText());
        assertTrue(captor.getValue().isRequired());
    }

    @Test
    void createJobQuestion_WhenJobNotDraft_ShouldThrowIllegalArgument() {
        when(jobOpeningRepository.existsByIdAndStatus(100L, JobOpeningStatus.DRAFT)).thenReturn(false);

        CreateJobQuestionRequest request = new CreateJobQuestionRequest("Q?", false);
        assertThrows(IllegalArgumentException.class,
                () -> recruiterJobService.createJobQuestion(request, 100L));
        verify(jobQuestionRepository, never()).save(any());
    }

    // ── getJobQuestions ─────────────────────────────────────────────────────

    @Test
    void getJobQuestions_WhenQuestionsExist_ShouldReturnMappedList() {
        JobQuestion q1 = JobQuestion.builder().questionText("Q1").required(true).build();
        TestEntityIdUtil.withId(q1, 601L);
        JobQuestion q2 = JobQuestion.builder().questionText("Q2").required(false).build();
        TestEntityIdUtil.withId(q2, 602L);

        when(jobQuestionRepository.findByJobOpeningId(100L)).thenReturn(List.of(q1, q2));

        List<JobQuestionResponse> result = recruiterJobService.getJobQuestions(100L);

        assertEquals(2, result.size());
        assertEquals("Q1", result.get(0).getQuestionText());
        assertTrue(result.get(0).isRequired());
        assertEquals("Q2", result.get(1).getQuestionText());
        assertFalse(result.get(1).isRequired());
    }

    @Test
    void getJobQuestions_WhenNoQuestions_ShouldReturnEmptyList() {
        when(jobQuestionRepository.findByJobOpeningId(100L)).thenReturn(List.of());

        List<JobQuestionResponse> result = recruiterJobService.getJobQuestions(100L);

        assertTrue(result.isEmpty());
    }

    // ── publishJob ──────────────────────────────────────────────────────────

    @Test
    void publishJob_WhenDraft_ShouldSetStatusToOpen() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.DRAFT).build();
        TestEntityIdUtil.withId(job, 100L);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        recruiterJobService.publishJob(100L);

        ArgumentCaptor<JobOpening> captor = ArgumentCaptor.forClass(JobOpening.class);
        verify(jobOpeningRepository).save(captor.capture());
        assertEquals(JobOpeningStatus.OPEN, captor.getValue().getStatus());
    }

    @Test
    void publishJob_WhenNotFound_ShouldThrowResourceNotFound() {
        when(jobOpeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recruiterJobService.publishJob(999L));
    }

    @Test
    void publishJob_WhenOpen_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.OPEN).build();
        TestEntityIdUtil.withId(job, 100L);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> recruiterJobService.publishJob(100L));
    }

    @Test
    void publishJob_WhenClosed_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.CLOSED).build();
        TestEntityIdUtil.withId(job, 100L);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> recruiterJobService.publishJob(100L));
    }

    // ── getApplicationsForJob ───────────────────────────────────────────────

    @Test
    void getApplicationsForJob_WhenApplicationsExist_ShouldReturnMappedList() {
        JobOpening jobOpening = JobOpening.builder().title("Dev").build();
        TestEntityIdUtil.withId(jobOpening, 100L);

        JobQuestion question = JobQuestion.builder().questionText("Why?").required(true).build();
        TestEntityIdUtil.withId(question, 300L);

        JobApplication app = JobApplication.builder()
                .jobOpening(jobOpening)
                .userId(42L)
                .resumeUrl("resume.pdf")
                .coverLetter("CL")
                .status(JobApplicationStatus.RECEIVED)
                .build();
        TestEntityIdUtil.withId(app, 200L);

        when(jobApplicationRepository.findByJobOpeningId(100L)).thenReturn(List.of(app));

        JobApplicationAnswer answer = new JobApplicationAnswer(app, question, "My answer");
        TestEntityIdUtil.withId(answer, 400L);
        when(jobApplicationAnswerRepository.findByApplicationId(200L)).thenReturn(List.of(answer));

        List<JobApplicationResponse> result = recruiterJobService.getApplicationsForJob(100L);

        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getJobApplicationId());
        assertEquals(100L, result.get(0).getJobOpeningId());
        assertEquals(42L, result.get(0).getUserId());
        assertEquals(1, result.get(0).getAnswers().size());
        assertEquals("My answer", result.get(0).getAnswers().get(0).getAnswer());
    }

    @Test
    void getApplicationsForJob_WhenNoApplications_ShouldReturnEmptyList() {
        when(jobApplicationRepository.findByJobOpeningId(100L)).thenReturn(List.of());

        List<JobApplicationResponse> result = recruiterJobService.getApplicationsForJob(100L);

        assertTrue(result.isEmpty());
    }

    // ── getApplicationTimeline ──────────────────────────────────────────────

    @Test
    void getApplicationTimeline_WhenHistoryExists_ShouldReturnMappedList() {
        JobApplicationStatusHistory h1 = new JobApplicationStatusHistory(
                200L, null, JobApplicationStatus.RECEIVED, "Submitted", 42L);
        JobApplicationStatusHistory h2 = new JobApplicationStatusHistory(
                200L, JobApplicationStatus.RECEIVED, JobApplicationStatus.REVIEWED, "Reviewed", 1L);

        when(jobApplicationStatusHistoryRepository.findByJobApplicationIdOrderByChangedDateAsc(200L))
                .thenReturn(List.of(h1, h2));

        List<JobApplicationTimelineResponse> result = recruiterJobService.getApplicationTimeline(200L);

        assertEquals(2, result.size());
        assertNull(result.get(0).getStatusFrom());
        assertEquals(JobApplicationStatus.RECEIVED, result.get(0).getStatusTo());
        assertEquals(JobApplicationStatus.RECEIVED, result.get(1).getStatusFrom());
        assertEquals(JobApplicationStatus.REVIEWED, result.get(1).getStatusTo());
    }

    @Test
    void getApplicationTimeline_WhenNoHistory_ShouldReturnEmptyList() {
        when(jobApplicationStatusHistoryRepository.findByJobApplicationIdOrderByChangedDateAsc(200L))
                .thenReturn(List.of());

        List<JobApplicationTimelineResponse> result = recruiterJobService.getApplicationTimeline(200L);

        assertTrue(result.isEmpty());
    }

    // ── closeJob ────────────────────────────────────────────────────────────

    @Test
    void closeJob_WhenJobIsOpen_ShouldSetStatusToClosed() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.OPEN).build();
        TestEntityIdUtil.withId(job, 100L);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        recruiterJobService.closeJob(100L);

        ArgumentCaptor<JobOpening> captor = ArgumentCaptor.forClass(JobOpening.class);
        verify(jobOpeningRepository).save(captor.capture());
        assertEquals(JobOpeningStatus.CLOSED, captor.getValue().getStatus());
    }

    @Test
    void closeJob_WhenJobNotFound_ShouldThrowResourceNotFound() {
        when(jobOpeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recruiterJobService.closeJob(999L));
    }

    @Test
    void closeJob_WhenJobIsDraft_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.DRAFT).build();
        TestEntityIdUtil.withId(job, 100L);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> recruiterJobService.closeJob(100L));
    }

    @Test
    void closeJob_WhenJobAlreadyClosed_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.CLOSED).build();
        TestEntityIdUtil.withId(job, 100L);
        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> recruiterJobService.closeJob(100L));
    }

    // ── reopenJob ───────────────────────────────────────────────────────────

    @Test
    void reopenJob_WhenJobIsClosed_ShouldSetStatusToOpen() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.CLOSED).build();
        TestEntityIdUtil.withId(job, 200L);
        when(jobOpeningRepository.findById(200L)).thenReturn(Optional.of(job));

        recruiterJobService.reopenJob(200L);

        ArgumentCaptor<JobOpening> captor = ArgumentCaptor.forClass(JobOpening.class);
        verify(jobOpeningRepository).save(captor.capture());
        assertEquals(JobOpeningStatus.OPEN, captor.getValue().getStatus());
    }

    @Test
    void reopenJob_WhenJobNotFound_ShouldThrowResourceNotFound() {
        when(jobOpeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recruiterJobService.reopenJob(999L));
    }

    @Test
    void reopenJob_WhenJobIsOpen_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.OPEN).build();
        TestEntityIdUtil.withId(job, 200L);
        when(jobOpeningRepository.findById(200L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> recruiterJobService.reopenJob(200L));
    }

    @Test
    void reopenJob_WhenJobIsDraft_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.DRAFT).build();
        TestEntityIdUtil.withId(job, 200L);
        when(jobOpeningRepository.findById(200L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> recruiterJobService.reopenJob(200L));
    }

    // ── changeApplicationStatus (audit trail) ───────────────────────────────

    @Test
    void changeApplicationStatus_WhenValidTransition_ShouldSaveHistoryWithFromAndTo() {
        JobApplication application = JobApplication.builder()
                .status(JobApplicationStatus.RECEIVED)
                .build();
        TestEntityIdUtil.withId(application, 300L);
        when(jobApplicationRepository.findById(300L)).thenReturn(Optional.of(application));

        ChangeApplicationStatusRequest request = new ChangeApplicationStatusRequest(
                JobApplicationStatus.REVIEWED, "Looks good");

        recruiterJobService.changeApplicationStatus(300L, request);

        ArgumentCaptor<JobApplicationStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(JobApplicationStatusHistory.class);
        verify(jobApplicationStatusHistoryRepository).save(historyCaptor.capture());

        JobApplicationStatusHistory savedHistory = historyCaptor.getValue();
        assertEquals(JobApplicationStatus.RECEIVED, savedHistory.getStatusFrom());
        assertEquals(JobApplicationStatus.REVIEWED, savedHistory.getStatusTo());
        assertEquals("Looks good", savedHistory.getNotes());
    }

    @Test
    void changeApplicationStatus_WhenInvalidTransition_ShouldThrowIllegalArgument() {
        JobApplication application = JobApplication.builder()
                .status(JobApplicationStatus.RECEIVED)
                .build();
        TestEntityIdUtil.withId(application, 300L);
        when(jobApplicationRepository.findById(300L)).thenReturn(Optional.of(application));

        ChangeApplicationStatusRequest request = new ChangeApplicationStatusRequest(
                JobApplicationStatus.HIRED, "Skip steps");

        assertThrows(IllegalArgumentException.class,
                () -> recruiterJobService.changeApplicationStatus(300L, request));
    }

    // ── updateJob ───────────────────────────────────────────────────────────

    private UpdateJobRequest buildUpdateRequest() {
        return new UpdateJobRequest(
                "Updated Title", "Updated Desc", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, java.util.List.of("Java", "Spring"));
    }

    @Test
    void updateJob_WhenDraft_ShouldUpdateFields() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.DRAFT).build();
        TestEntityIdUtil.withId(job, 400L);
        when(jobOpeningRepository.findById(400L)).thenReturn(Optional.of(job));

        recruiterJobService.updateJob(400L, buildUpdateRequest());

        ArgumentCaptor<JobOpening> captor = ArgumentCaptor.forClass(JobOpening.class);
        verify(jobOpeningRepository).save(captor.capture());
        assertEquals("Updated Title", captor.getValue().getTitle());
        assertEquals("Updated Desc", captor.getValue().getDescription());
        assertEquals(JobType.FULL_TIME, captor.getValue().getJobType());
    }

    @Test
    void updateJob_WhenNotFound_ShouldThrowResourceNotFound() {
        when(jobOpeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recruiterJobService.updateJob(999L, buildUpdateRequest()));
    }

    @Test
    void updateJob_WhenOpen_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.OPEN).build();
        TestEntityIdUtil.withId(job, 400L);
        when(jobOpeningRepository.findById(400L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> recruiterJobService.updateJob(400L, buildUpdateRequest()));
    }

    @Test
    void updateJob_WhenClosed_ShouldThrowInvalidJobState() {
        JobOpening job = JobOpening.builder().status(JobOpeningStatus.CLOSED).build();
        TestEntityIdUtil.withId(job, 400L);
        when(jobOpeningRepository.findById(400L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> recruiterJobService.updateJob(400L, buildUpdateRequest()));
    }
}
