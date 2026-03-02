package com.nifilili.job.service.impl;

import com.nifilili.core.enums.job.JobApplicationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.nifilili.core.enums.job.JobApplicationStatus.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Exhaustive tests for the status transition helper method.
 * Covers every valid and invalid transition in the recruitment pipeline.
 */
class JobApplicationStatusTransitionTest {

    // ── Valid transitions ────────────────────────────────────────────────────

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(RECEIVED, REVIEWED),
                Arguments.of(RECEIVED, REJECTED),
                Arguments.of(REVIEWED, SHORTLISTED),
                Arguments.of(REVIEWED, REJECTED),
                Arguments.of(SHORTLISTED, INTERVIEWING),
                Arguments.of(INTERVIEWING, HIRED),
                Arguments.of(INTERVIEWING, REJECTED)
        );
    }

    @ParameterizedTest(name = "{0} -> {1} should be allowed")
    @MethodSource("validTransitions")
    void isJobApplicationStatusAllowed_WhenValidTransition_ShouldReturnTrue(
            JobApplicationStatus from, JobApplicationStatus to) {
        assertTrue(RecruiterJobServiceImpl.isJobApplicationStatusAllowed(from, to));
    }

    // ── Invalid transitions ─────────────────────────────────────────────────

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                // RECEIVED: cannot skip to SHORTLISTED, INTERVIEWING, HIRED, WITHDRAWN
                Arguments.of(RECEIVED, SHORTLISTED),
                Arguments.of(RECEIVED, INTERVIEWING),
                Arguments.of(RECEIVED, HIRED),
                Arguments.of(RECEIVED, WITHDRAWN),
                Arguments.of(RECEIVED, RECEIVED),

                // REVIEWED: cannot go back or skip
                Arguments.of(REVIEWED, RECEIVED),
                Arguments.of(REVIEWED, REVIEWED),
                Arguments.of(REVIEWED, INTERVIEWING),
                Arguments.of(REVIEWED, HIRED),
                Arguments.of(REVIEWED, WITHDRAWN),

                // SHORTLISTED: can only go to INTERVIEWING
                Arguments.of(SHORTLISTED, RECEIVED),
                Arguments.of(SHORTLISTED, REVIEWED),
                Arguments.of(SHORTLISTED, SHORTLISTED),
                Arguments.of(SHORTLISTED, HIRED),
                Arguments.of(SHORTLISTED, REJECTED),
                Arguments.of(SHORTLISTED, WITHDRAWN),

                // INTERVIEWING: can only go to HIRED or REJECTED
                Arguments.of(INTERVIEWING, RECEIVED),
                Arguments.of(INTERVIEWING, REVIEWED),
                Arguments.of(INTERVIEWING, SHORTLISTED),
                Arguments.of(INTERVIEWING, INTERVIEWING),
                Arguments.of(INTERVIEWING, WITHDRAWN),

                // Terminal states: HIRED, REJECTED, WITHDRAWN cannot transition
                Arguments.of(HIRED, RECEIVED),
                Arguments.of(HIRED, REVIEWED),
                Arguments.of(HIRED, REJECTED),
                Arguments.of(REJECTED, RECEIVED),
                Arguments.of(REJECTED, REVIEWED),
                Arguments.of(REJECTED, HIRED),
                Arguments.of(WITHDRAWN, RECEIVED),
                Arguments.of(WITHDRAWN, REVIEWED),
                Arguments.of(WITHDRAWN, HIRED)
        );
    }

    @ParameterizedTest(name = "{0} -> {1} should NOT be allowed")
    @MethodSource("invalidTransitions")
    void isJobApplicationStatusAllowed_WhenInvalidTransition_ShouldReturnFalse(
            JobApplicationStatus from, JobApplicationStatus to) {
        assertFalse(RecruiterJobServiceImpl.isJobApplicationStatusAllowed(from, to));
    }

    // ── Specific edge cases ─────────────────────────────────────────────────

    @Test
    void shortlisted_CannotBeRejected_ShouldReturnFalse() {
        // Business rule: SHORTLISTED can only go to INTERVIEWING, not directly rejected
        assertFalse(RecruiterJobServiceImpl.isJobApplicationStatusAllowed(SHORTLISTED, REJECTED));
    }

    @Test
    void hiredIsTerminal_ShouldNotAllowAnyTransition() {
        for (JobApplicationStatus target : JobApplicationStatus.values()) {
            assertFalse(RecruiterJobServiceImpl.isJobApplicationStatusAllowed(HIRED, target));
        }
    }

    @Test
    void rejectedIsTerminal_ShouldNotAllowAnyTransition() {
        for (JobApplicationStatus target : JobApplicationStatus.values()) {
            assertFalse(RecruiterJobServiceImpl.isJobApplicationStatusAllowed(REJECTED, target));
        }
    }

    @Test
    void withdrawnIsTerminal_ShouldNotAllowAnyTransition() {
        for (JobApplicationStatus target : JobApplicationStatus.values()) {
            assertFalse(RecruiterJobServiceImpl.isJobApplicationStatusAllowed(WITHDRAWN, target));
        }
    }
}
