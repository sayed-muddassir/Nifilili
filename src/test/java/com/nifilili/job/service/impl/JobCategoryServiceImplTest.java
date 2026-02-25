package com.nifilili.job.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.job.domain.JobCategory;
import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.response.JobCategoryResponse;
import com.nifilili.job.repository.JobCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCategoryServiceImplTest {

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @InjectMocks
    private JobCategoryServiceImpl jobCategoryService;

    // ── createCategory ──────────────────────────────────────────────────────

    @Test
    void createCategory_WhenUniqueName_ShouldSaveAndReturnId() {
        when(jobCategoryRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);

        CreateJobCategoryRequest request = new CreateJobCategoryRequest("Engineering");
        Long result = jobCategoryService.createCategory(request, 1L);

        ArgumentCaptor<JobCategory> captor = ArgumentCaptor.forClass(JobCategory.class);
        verify(jobCategoryRepository).save(captor.capture());
        assertEquals("Engineering", captor.getValue().getName());
        assertEquals(1L, captor.getValue().getCreatedBy());
    }

    @Test
    void createCategory_WhenDuplicateName_ShouldThrowIllegalArgument() {
        when(jobCategoryRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

        CreateJobCategoryRequest request = new CreateJobCategoryRequest("Engineering");

        assertThrows(IllegalArgumentException.class,
                () -> jobCategoryService.createCategory(request, 1L));
        verify(jobCategoryRepository, never()).save(any());
    }

    // ── getAllCategories ─────────────────────────────────────────────────────

    @Test
    void getAllCategories_WhenCategoriesExist_ShouldReturnMappedList() {
        JobCategory cat1 = new JobCategory("Engineering", 1L);
        TestEntityIdUtil.withId(cat1, 10L);

        JobCategory cat2 = new JobCategory("Marketing", 1L);
        TestEntityIdUtil.withId(cat2, 11L);

        when(jobCategoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<JobCategoryResponse> result = jobCategoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("Engineering", result.get(0).name());
        assertEquals(11L, result.get(1).id());
        assertEquals("Marketing", result.get(1).name());
    }

    @Test
    void getAllCategories_WhenNoCategoriesExist_ShouldReturnEmptyList() {
        when(jobCategoryRepository.findAll()).thenReturn(List.of());

        List<JobCategoryResponse> result = jobCategoryService.getAllCategories();

        assertTrue(result.isEmpty());
    }
}
