package com.nifilili.job.service;

import com.nifilili.job.dto.request.ChangeApplicationStatusRequest;
import com.nifilili.job.dto.request.CreateJobQuestionRequest;
import com.nifilili.job.dto.request.CreateJobRequest;
import com.nifilili.job.dto.request.UpdateJobRequest;
import com.nifilili.job.dto.response.JobApplicationResponse;
import com.nifilili.job.dto.response.JobApplicationTimelineResponse;
import com.nifilili.job.dto.response.JobQuestionResponse;

import java.util.List;

public interface RecruiterJobService {

    /**
     * Creates a new job opening in DRAFT status for the specified business.
     *
     * @param request the job creation details including business ID, category, title, description, and location
     * @return the ID of the newly created job opening
     * @throws IllegalArgumentException if the business ID is invalid or the job category does not exist
     */
    Long createJob(CreateJobRequest request);

    /**
     * Updates a job opening that is still in DRAFT status.
     *
     * @param jobId   the ID of the job opening to update
     * @param request the updated job details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if the job opening does not exist
     * @throws com.nifilili.core.exception.InvalidJobStateException  if the job is not in DRAFT status
     */
    void updateJob(Long jobId, UpdateJobRequest request);

    /**
     * Adds a screening question to a DRAFT job opening.
     *
     * @param request the question details including text and whether it is required
     * @param jobId   the ID of the job opening to add the question to
     * @return the ID of the newly created screening question
     * @throws IllegalArgumentException if the job is not found or not in DRAFT status
     */
    Long createJobQuestion(CreateJobQuestionRequest request, Long jobId);

    /**
     * Retrieves all screening questions for a specific job opening.
     *
     * @param jobId the ID of the job opening
     * @return list of screening questions with their IDs, text, and required flag
     */
    List<JobQuestionResponse> getJobQuestions(Long jobId);

    /**
     * Publishes a DRAFT job opening, changing its status to OPEN and making it visible to applicants.
     *
     * @param jobId the ID of the job opening to publish
     * @throws com.nifilili.core.exception.ResourceNotFoundException if the job opening does not exist
     * @throws com.nifilili.core.exception.InvalidJobStateException  if the job is not in DRAFT status
     */
    void publishJob(Long jobId);

    /**
     * Closes an open job opening, hiding it from public listings and preventing new applications.
     *
     * @param jobId the ID of the job opening to close
     * @throws com.nifilili.core.exception.ResourceNotFoundException if the job opening does not exist
     * @throws com.nifilili.core.exception.InvalidJobStateException  if the job is not in OPEN status
     */
    void closeJob(Long jobId);

    /**
     * Reopens a previously closed job opening, making it visible to public listings again.
     *
     * @param jobId the ID of the job opening to reopen
     * @throws com.nifilili.core.exception.ResourceNotFoundException if the job opening does not exist
     * @throws com.nifilili.core.exception.InvalidJobStateException  if the job is not in CLOSED status
     */
    void reopenJob(Long jobId);

    /**
     * Retrieves all applications submitted for a specific job opening with answers to screening questions.
     *
     * @param jobId the ID of the job opening
     * @return list of applications including resume, cover letter, status, and screening answers
     */
    List<JobApplicationResponse> getApplicationsForJob(Long jobId);

    /**
     * Moves an application through the recruitment pipeline. Validates that the status transition is allowed.
     *
     * @param applicationId the ID of the application to update
     * @param request       the new status and optional notes
     * @throws IllegalArgumentException if the application does not exist or the status transition is invalid
     */
    void changeApplicationStatus(Long applicationId, ChangeApplicationStatusRequest request);

    /**
     * Retrieves the full audit trail of status changes for a specific application.
     *
     * @param applicationId the ID of the application
     * @return chronological list of status transitions with timestamps, actors, and notes
     */
    List<JobApplicationTimelineResponse> getApplicationTimeline(Long applicationId);

}
