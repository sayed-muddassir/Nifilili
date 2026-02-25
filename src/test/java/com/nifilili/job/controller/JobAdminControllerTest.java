package com.nifilili.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.core.exception.GlobalExceptionHandler;
import com.nifilili.job.controller.admin.JobAdminController;
import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;
import com.nifilili.job.service.JobCategoryService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobAdminControllerTest {

    @Mock
    private JobCategoryService jobCategoryService;

    @InjectMocks
    private JobAdminController controller;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── createCategory ──────────────────────────────────────────────────────

    @Test
    void createCategory_WhenValid_ShouldReturn201WithId() throws Exception {
        when(jobCategoryService.createCategory(any(CreateJobCategoryRequest.class), eq(1L)))
                .thenReturn(10L);

        mvc.perform(post("/api/v1/admin/jobs/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Engineering\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"));
    }

    @Test
    void createCategory_WhenNameBlank_ShouldReturn400() throws Exception {
        mvc.perform(post("/api/v1/admin/jobs/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());

        verify(jobCategoryService, never()).createCategory(any(), anyLong());
    }

    @Test
    void createCategory_WhenDuplicate_ShouldReturn400() throws Exception {
        when(jobCategoryService.createCategory(any(CreateJobCategoryRequest.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Job category already exists"));

        mvc.perform(post("/api/v1/admin/jobs/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Engineering\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── getAllCategories ─────────────────────────────────────────────────────

    @Test
    void getAllCategories_ShouldReturn200WithList() throws Exception {
        when(jobCategoryService.getAllCategories()).thenReturn(List.of(
                new JobCategoryResponse(10L, "Engineering", Instant.now()),
                new JobCategoryResponse(11L, "Marketing", Instant.now())));

        mvc.perform(get("/api/v1/admin/jobs/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Engineering"))
                .andExpect(jsonPath("$[1].name").value("Marketing"));
    }

    @Test
    void getAllCategories_WhenEmpty_ShouldReturn200WithEmptyList() throws Exception {
        when(jobCategoryService.getAllCategories()).thenReturn(List.of());

        mvc.perform(get("/api/v1/admin/jobs/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
