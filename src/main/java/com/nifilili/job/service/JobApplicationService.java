package com.nifilili.job.service;

import com.nifilili.job.dto.request.ApplyJobRequest;
import com.nifilili.job.dto.response.MyApplicationResponse;

import java.util.List;

public interface JobApplicationService {

    /**
     * Submits a job application for the given job opening.
     *
     * @param jobId   the ID of the job opening to apply for
     * @param request the application details including resume, cover letter, and screening answers
     * @return the ID of the newly created application
     * @throws IllegalArgumentException if the job is not open or the user has already applied
     */
    Long apply(Long jobId, ApplyJobRequest request);

    /**
     * Withdraws a job application. Withdrawal is only permitted while the application
     * is still in RECEIVED status (before the recruiter moves it to REVIEWED).
     *
     * @param applicationId the ID of the application to withdraw
     * @param reason        the reason for withdrawal
     * @throws IllegalArgumentException if the application does not exist
     * @throws IllegalStateException    if the application status is not RECEIVED
     */
    void withdraw(Long applicationId, String reason);

    /**
     * Retrieves all job applications for the given user.
     *
     * @param userId the ID of the user
     * @return list of the user's applications with current status
     */
    List<MyApplicationResponse> getMyApplications(Long userId);
}
