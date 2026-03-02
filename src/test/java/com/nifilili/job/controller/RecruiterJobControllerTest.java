package com.nifilili.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.enums.job.JobType;
import com.nifilili.core.exception.GlobalExceptionHandler;
import com.nifilili.core.exception.InvalidJobStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.job.controller.owner.RecruiterJobController;
import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.request.UpdateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;
import com.nifilili.job.service.RecruiterJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecruiterJobControllerTest {

    @Mock
    private RecruiterJobService recruiterJobService;

    @InjectMocks
    private RecruiterJobController controller;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── createJob ───────────────────────────────────────────────────────────

    @Test
    void createJob_WhenValid_ShouldReturn201WithId() throws Exception {
        when(recruiterJobService.createJob(any(CreateJobRequest.class))).thenReturn(100L);

        CreateJobRequest request = new CreateJobRequest(
                1L, 10L, "Backend Dev", "Java role", JobType.FULL_TIME,
                5L, 3L, "Tole", "44600", false,
                50000.0, 80000.0, 2, LocalDate.now().plusDays(30),
                List.of("Java"));

        mvc.perform(post("/api/v1/business/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("100"));

        verify(recruiterJobService).createJob(any(CreateJobRequest.class));
    }

    // ── updateJob ───────────────────────────────────────────────────────────

    @Test
    void updateJob_WhenValid_ShouldReturn200() throws Exception {
        doNothing().when(recruiterJobService).updateJob(eq(100L), any(UpdateJobRequest.class));

        UpdateJobRequest request = new UpdateJobRequest(
                "Updated", "Desc", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));

        mvc.perform(put("/api/v1/business/jobs/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recruiterJobService).updateJob(eq(100L), any(UpdateJobRequest.class));
    }

    @Test
    void updateJob_WhenTitleBlank_ShouldReturn400() throws Exception {
        UpdateJobRequest request = new UpdateJobRequest(
                "", "Desc", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));

        mvc.perform(put("/api/v1/business/jobs/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(recruiterJobService, never()).updateJob(anyLong(), any());
    }

    @Test
    void updateJob_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Job not found"))
                .when(recruiterJobService).updateJob(eq(999L), any(UpdateJobRequest.class));

        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "Desc", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));

        mvc.perform(put("/api/v1/business/jobs/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateJob_WhenNotDraft_ShouldReturn400() throws Exception {
        doThrow(new InvalidJobStateException("Only DRAFT jobs can be updated"))
                .when(recruiterJobService).updateJob(eq(100L), any(UpdateJobRequest.class));

        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "Desc", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));

        mvc.perform(put("/api/v1/business/jobs/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── publishJob ──────────────────────────────────────────────────────────

    @Test
    void publishJob_WhenValid_ShouldReturn200() throws Exception {
        doNothing().when(recruiterJobService).publishJob(100L);

        mvc.perform(post("/api/v1/business/jobs/100/publish"))
                .andExpect(status().isOk());

        verify(recruiterJobService).publishJob(100L);
    }

    // ── closeJob ────────────────────────────────────────────────────────────

    @Test
    void closeJob_WhenValid_ShouldReturn200() throws Exception {
        doNothing().when(recruiterJobService).closeJob(100L);

        mvc.perform(post("/api/v1/business/jobs/100/close"))
                .andExpect(status().isOk());

        verify(recruiterJobService).closeJob(100L);
    }

    @Test
    void closeJob_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Job not found"))
                .when(recruiterJobService).closeJob(999L);

        mvc.perform(post("/api/v1/business/jobs/999/close"))
                .andExpect(status().isNotFound());
    }

    // ── reopenJob ───────────────────────────────────────────────────────────

    @Test
    void reopenJob_WhenValid_ShouldReturn200() throws Exception {
        doNothing().when(recruiterJobService).reopenJob(100L);

        mvc.perform(post("/api/v1/business/jobs/100/reopen"))
                .andExpect(status().isOk());

        verify(recruiterJobService).reopenJob(100L);
    }

    // ── createJobQuestions ──────────────────────────────────────────────────

    @Test
    void createJobQuestions_WhenValid_ShouldReturn201WithId() throws Exception {
        when(recruiterJobService.createJobQuestion(any(CreateJobQuestionRequest.class), eq(100L)))
                .thenReturn(600L);

        CreateJobQuestionRequest request = new CreateJobQuestionRequest("Why us?", true);

        mvc.perform(post("/api/v1/business/jobs/100/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("600"));
    }

    // ── getJobQuestions ─────────────────────────────────────────────────────

    @Test
    void getJobQuestions_ShouldReturn200WithList() throws Exception {
        when(recruiterJobService.getJobQuestions(100L)).thenReturn(List.of(
                JobQuestionResponse.builder().id(1L).questionText("Q1").required(true).build()));

        mvc.perform(get("/api/v1/business/jobs/100/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].questionText").value("Q1"));
    }

    // ── getApplications ─────────────────────────────────────────────────────

    @Test
    void getApplications_ShouldReturn200WithList() throws Exception {
        when(recruiterJobService.getApplicationsForJob(100L)).thenReturn(List.of(
                JobApplicationResponse.builder()
                        .jobApplicationId(200L)
                        .jobOpeningId(100L)
                        .userId(42L)
                        .status(JobApplicationStatus.RECEIVED)
                        .answers(List.of())
                        .build()));

        mvc.perform(get("/api/v1/business/jobs/100/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobApplicationId").value(200))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"));
    }

    // ── changeStatus ────────────────────────────────────────────────────────

    @Test
    void changeStatus_WhenValid_ShouldReturn200() throws Exception {
        doNothing().when(recruiterJobService)
                .changeApplicationStatus(eq(200L), any(ChangeApplicationStatusRequest.class));

        ChangeApplicationStatusRequest request =
                new ChangeApplicationStatusRequest(JobApplicationStatus.REVIEWED, "Looks good");

        mvc.perform(post("/api/v1/business/jobs/applications/200/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changeStatus_WhenInvalidTransition_ShouldReturn400() throws Exception {
        doThrow(new IllegalArgumentException("Invalid status transition"))
                .when(recruiterJobService)
                .changeApplicationStatus(eq(200L), any(ChangeApplicationStatusRequest.class));

        ChangeApplicationStatusRequest request =
                new ChangeApplicationStatusRequest(JobApplicationStatus.HIRED, "Skip");

        mvc.perform(post("/api/v1/business/jobs/applications/200/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── getApplicationTimeline ──────────────────────────────────────────────

    @Test
    void getApplicationTimeline_ShouldReturn200WithList() throws Exception {
        when(recruiterJobService.getApplicationTimeline(200L)).thenReturn(List.of(
                JobApplicationTimelineResponse.builder()
                        .statusFrom(null)
                        .statusTo(JobApplicationStatus.RECEIVED)
                        .changedBy(42L)
                        .changedDate(Instant.now())
                        .notes("Submitted")
                        .build()));

        mvc.perform(get("/api/v1/business/jobs/applications/200/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statusTo").value("RECEIVED"))
                .andExpect(jsonPath("$[0].notes").value("Submitted"));
    }
}
