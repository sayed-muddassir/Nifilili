package com.nifilili.job.controller;

import com.nifilili.core.enums.job.JobType;
import com.nifilili.core.exception.GlobalExceptionHandler;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.job.controller.publicapi.PublicJobController;
import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;
import com.nifilili.job.service.JobQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicJobControllerTest {

    @Mock
    private JobQueryService jobQueryService;

    @InjectMocks
    private PublicJobController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── searchJobs ──────────────────────────────────────────────────────────

    @Test
    void searchJobs_WhenResultsExist_ShouldReturn200WithList() throws Exception {
        JobSummaryResponse summary = new JobSummaryResponse();
        summary.setJobId(100L);
        summary.setTitle("Backend Dev");
        summary.setJobType(JobType.FULL_TIME);
        when(jobQueryService.searchJobs("Backend", null)).thenReturn(List.of(summary));

        mvc.perform(get("/api/v1/public/jobs")
                        .param("keyword", "Backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value(100))
                .andExpect(jsonPath("$[0].title").value("Backend Dev"));
    }

    @Test
    void searchJobs_WhenNoParams_ShouldReturn200() throws Exception {
        when(jobQueryService.searchJobs(null, null)).thenReturn(List.of());

        mvc.perform(get("/api/v1/public/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchJobs_WhenFilteredByCategory_ShouldPassCategoryId() throws Exception {
        when(jobQueryService.searchJobs(null, 5L)).thenReturn(List.of());

        mvc.perform(get("/api/v1/public/jobs")
                        .param("categoryId", "5"))
                .andExpect(status().isOk());
    }

    // ── getJobDetails ───────────────────────────────────────────────────────

    @Test
    void getJobDetails_WhenFound_ShouldReturn200WithDetails() throws Exception {
        JobDetailsResponse details = new JobDetailsResponse();
        details.setTitle("Backend Dev");
        details.setDescription("Java developer");
        details.setJobType(JobType.FULL_TIME);
        details.setRemote(true);
        when(jobQueryService.getJobDetails(100L)).thenReturn(details);

        mvc.perform(get("/api/v1/public/jobs/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Dev"))
                .andExpect(jsonPath("$.remote").value(true));
    }

    @Test
    void getJobDetails_WhenNotFound_ShouldReturn404() throws Exception {
        when(jobQueryService.getJobDetails(999L))
                .thenThrow(new ResourceNotFoundException("Job opening not found with id: 999"));

        mvc.perform(get("/api/v1/public/jobs/999"))
                .andExpect(status().isNotFound());
    }
}
