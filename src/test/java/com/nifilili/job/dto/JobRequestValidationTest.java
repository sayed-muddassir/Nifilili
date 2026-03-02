package com.nifilili.job.dto;

import com.nifilili.core.enums.job.JobType;
import com.nifilili.job.dto.request.CreateJobCategoryRequest;
import com.nifilili.job.dto.request.UpdateJobRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JobRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ── CreateJobCategoryRequest ────────────────────────────────────────────

    @Test
    void createJobCategoryRequest_WhenValid_ShouldPassValidation() {
        CreateJobCategoryRequest request = new CreateJobCategoryRequest("Engineering");
        Set<ConstraintViolation<CreateJobCategoryRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void createJobCategoryRequest_WhenNameBlank_ShouldFailValidation() {
        CreateJobCategoryRequest request = new CreateJobCategoryRequest("");
        Set<ConstraintViolation<CreateJobCategoryRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void createJobCategoryRequest_WhenNameNull_ShouldFailValidation() {
        CreateJobCategoryRequest request = new CreateJobCategoryRequest(null);
        Set<ConstraintViolation<CreateJobCategoryRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    // ── UpdateJobRequest ────────────────────────────────────────────────────

    @Test
    void updateJobRequest_WhenValid_ShouldPassValidation() {
        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "Description", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));
        Set<ConstraintViolation<UpdateJobRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void updateJobRequest_WhenTitleBlank_ShouldFailValidation() {
        UpdateJobRequest request = new UpdateJobRequest(
                "", "Description", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));
        Set<ConstraintViolation<UpdateJobRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void updateJobRequest_WhenDescriptionBlank_ShouldFailValidation() {
        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "", JobType.FULL_TIME,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));
        Set<ConstraintViolation<UpdateJobRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void updateJobRequest_WhenJobTypeNull_ShouldFailValidation() {
        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "Description", null,
                1L, 2L, "Tole", "44600", false,
                50000.0, 80000.0, 3, null, List.of("Java"));
        Set<ConstraintViolation<UpdateJobRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("jobType")));
    }

    @Test
    void updateJobRequest_WhenOptionalFieldsNull_ShouldPassValidation() {
        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "Description", JobType.CONTRACT,
                null, null, null, null, true,
                null, null, null, null, null);
        Set<ConstraintViolation<UpdateJobRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ── DTO accessor smoke tests ────────────────────────────────────────────

    @Test
    void updateJobRequest_Accessors_ShouldReturnAllValues() {
        UpdateJobRequest request = new UpdateJobRequest(
                "Title", "Desc", JobType.INTERNSHIP,
                1L, 2L, "Tole", "44600", true,
                30000.0, 50000.0, 5, null, List.of("Python"));

        assertEquals("Title", request.title());
        assertEquals("Desc", request.description());
        assertEquals(JobType.INTERNSHIP, request.jobType());
        assertEquals(1L, request.municipalityId());
        assertEquals(2L, request.wardNumber());
        assertEquals("Tole", request.toleName());
        assertEquals("44600", request.postalCode());
        assertTrue(request.remote());
        assertEquals(30000.0, request.salaryRangeMin());
        assertEquals(50000.0, request.salaryRangeMax());
        assertEquals(5, request.numberOfOpenings());
        assertEquals(List.of("Python"), request.skills());
    }

    @Test
    void createJobCategoryRequest_Accessor_ShouldReturnName() {
        CreateJobCategoryRequest request = new CreateJobCategoryRequest("Tech");
        assertEquals("Tech", request.name());
    }
}
