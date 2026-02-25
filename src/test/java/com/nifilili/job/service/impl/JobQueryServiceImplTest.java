package com.nifilili.job.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.job.JobOpeningStatus;
import com.nifilili.core.enums.job.JobType;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.job.domain.JobCategory;
import com.nifilili.job.domain.JobOpening;
import com.nifilili.job.dto.response.JobDetailsResponse;
import com.nifilili.job.dto.response.JobSummaryResponse;
import com.nifilili.job.repository.JobOpeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobQueryServiceImplTest {

    @Mock
    private JobOpeningRepository jobOpeningRepository;

    @InjectMocks
    private JobQueryServiceImpl jobQueryService;

    // ── searchJobs ──────────────────────────────────────────────────────────

    @Test
    void searchJobs_WhenMatchesExist_ShouldReturnMappedSummaries() {
        JobOpening job1 = JobOpening.builder()
                .title("Backend Dev")
                .description("Java role")
                .jobType(JobType.FULL_TIME)
                .status(JobOpeningStatus.OPEN)
                .build();
        TestEntityIdUtil.withId(job1, 100L);

        JobOpening job2 = JobOpening.builder()
                .title("Frontend Dev")
                .description("React role")
                .jobType(JobType.CONTRACT)
                .status(JobOpeningStatus.OPEN)
                .build();
        TestEntityIdUtil.withId(job2, 101L);

        when(jobOpeningRepository.searchOpenJobs("Dev", null)).thenReturn(List.of(job1, job2));

        List<JobSummaryResponse> result = jobQueryService.searchJobs("Dev", null);

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getJobId());
        assertEquals("Backend Dev", result.get(0).getTitle());
        assertEquals(JobType.FULL_TIME, result.get(0).getJobType());
        assertEquals(101L, result.get(1).getJobId());
    }

    @Test
    void searchJobs_WhenNoMatches_ShouldReturnEmptyList() {
        when(jobOpeningRepository.searchOpenJobs("nonexistent", null)).thenReturn(List.of());

        List<JobSummaryResponse> result = jobQueryService.searchJobs("nonexistent", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchJobs_WhenFilteredByCategory_ShouldPassCategoryIdToRepo() {
        when(jobOpeningRepository.searchOpenJobs(null, 5L)).thenReturn(List.of());

        List<JobSummaryResponse> result = jobQueryService.searchJobs(null, 5L);

        assertTrue(result.isEmpty());
    }

    // ── getJobDetails ───────────────────────────────────────────────────────

    @Test
    void getJobDetails_WhenJobExists_ShouldReturnFullDetails() {
        JobCategory category = new JobCategory("Engineering", 1L);
        TestEntityIdUtil.withId(category, 10L);

        JobOpening job = JobOpening.builder()
                .title("Backend Dev")
                .description("Java developer")
                .category(category)
                .jobType(JobType.FULL_TIME)
                .municipalityId(5L)
                .wardNumber(3L)
                .toleName("Tole1")
                .postalCode("44600")
                .remote(true)
                .salaryRangeMin(50000.0)
                .salaryRangeMax(80000.0)
                .numberOfOpenings(2)
                .skills("[\"Java\",\"Spring\"]")
                .build();
        TestEntityIdUtil.withId(job, 100L);

        when(jobOpeningRepository.findById(100L)).thenReturn(Optional.of(job));

        JobDetailsResponse result = jobQueryService.getJobDetails(100L);

        assertEquals("Backend Dev", result.getTitle());
        assertEquals("Java developer", result.getDescription());
        assertEquals(10L, result.getCategoryId());
        assertEquals("Engineering", result.getCategoryName());
        assertEquals(JobType.FULL_TIME, result.getJobType());
        assertTrue(result.isRemote());
        assertEquals(50000.0, result.getSalaryRangeMin());
        assertEquals(2, result.getNumberOfOpenings());
    }

    @Test
    void getJobDetails_WhenJobNotFound_ShouldThrowResourceNotFound() {
        when(jobOpeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobQueryService.getJobDetails(999L));
    }
}
