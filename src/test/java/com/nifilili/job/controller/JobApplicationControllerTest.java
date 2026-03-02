package com.nifilili.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.core.enums.job.JobApplicationStatus;
import com.nifilili.core.exception.GlobalExceptionHandler;
import com.nifilili.job.controller.publicapi.JobApplicationController;
import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;
import com.nifilili.job.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private JobApplicationController controller;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── applyForJob ─────────────────────────────────────────────────────────

    @Test
    void applyForJob_WhenValid_ShouldReturn201WithId() throws Exception {
        when(jobApplicationService.apply(eq(100L), any(ApplyJobRequest.class))).thenReturn(200L);

        ApplyJobRequest request = new ApplyJobRequest(42L, "resume.pdf", "Cover", Map.of(1L, "Answer"));

        mvc.perform(post("/api/v1/public/jobs/100/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("200"));

        verify(jobApplicationService).apply(eq(100L), any(ApplyJobRequest.class));
    }

    @Test
    void applyForJob_WhenJobNotOpen_ShouldReturn400() throws Exception {
        when(jobApplicationService.apply(eq(100L), any(ApplyJobRequest.class)))
                .thenThrow(new IllegalArgumentException("Job is not open for applications"));

        ApplyJobRequest request = new ApplyJobRequest(42L, "resume.pdf", "Cover", Map.of());

        mvc.perform(post("/api/v1/public/jobs/100/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void applyForJob_WhenDuplicateApplication_ShouldReturn400() throws Exception {
        when(jobApplicationService.apply(eq(100L), any(ApplyJobRequest.class)))
                .thenThrow(new IllegalArgumentException("User has already applied for this job"));

        ApplyJobRequest request = new ApplyJobRequest(42L, "resume.pdf", "Cover", Map.of());

        mvc.perform(post("/api/v1/public/jobs/100/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── withdrawApplication ─────────────────────────────────────────────────

    @Test
    void withdrawApplication_WhenValid_ShouldReturn200() throws Exception {
        doNothing().when(jobApplicationService).withdraw(200L, "Changed my mind");

        mvc.perform(post("/api/v1/public/jobs/applications/200/withdraw")
                        .param("reason", "Changed my mind"))
                .andExpect(status().isOk());

        verify(jobApplicationService).withdraw(200L, "Changed my mind");
    }

    @Test
    void withdrawApplication_WhenNotReceived_ShouldReturn500() throws Exception {
        doThrow(new IllegalStateException("Withdrawal is only allowed before the application is reviewed"))
                .when(jobApplicationService).withdraw(eq(200L), any());

        mvc.perform(post("/api/v1/public/jobs/applications/200/withdraw")
                        .param("reason", "Too late"))
                .andExpect(status().isInternalServerError());
    }

    // ── myApplications ──────────────────────────────────────────────────────

    @Test
    void myApplications_ShouldReturn200WithList() throws Exception {
        when(jobApplicationService.getMyApplications(42L)).thenReturn(List.of(
                MyApplicationResponse.builder()
                        .applicationId(200L)
                        .jobTitle("Backend Dev")
                        .jobDescription("Java role")
                        .applicationStatus(JobApplicationStatus.RECEIVED)
                        .build()));

        mvc.perform(get("/api/v1/public/jobs/applications/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(200))
                .andExpect(jsonPath("$[0].jobTitle").value("Backend Dev"))
                .andExpect(jsonPath("$[0].applicationStatus").value("RECEIVED"));
    }

    @Test
    void myApplications_WhenEmpty_ShouldReturn200WithEmptyList() throws Exception {
        when(jobApplicationService.getMyApplications(42L)).thenReturn(List.of());

        mvc.perform(get("/api/v1/public/jobs/applications/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
