package com.nifilili.quote.service;

import com.nifilili.quote.dto.request.CreateQuoteRequestRequest;
import com.nifilili.quote.dto.response.QuoteRequestResponse;

import java.util.List;

public interface QuoteRequestService {

    /**
     * Creates a new quote request for a service offering with dynamic pricing.
     *
     * @param request the quote request details including offering, requirements, and optional delivery address
     * @return the created quote request
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if offering validation fails
     */
    QuoteRequestResponse createRequest(CreateQuoteRequestRequest request);

    /**
     * Retrieves a quote request by ID with authorization check.
     *
     * @param requestId the quote request ID
     * @return the quote request details
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     */
    QuoteRequestResponse getRequest(Long requestId);

    /**
     * Lists all quote requests submitted by the authenticated user.
     *
     * @return list of quote requests ordered by most recent first
     */
    List<QuoteRequestResponse> listMyRequests();

    /**
     * Lists all quote requests received by a specific business.
     *
     * @param businessId the business ID
     * @return list of quote requests ordered by most recent first
     */
    List<QuoteRequestResponse> listBusinessRequests(Long businessId);

    /**
     * Declines a pending quote request with a reason.
     *
     * @param requestId the quote request ID
     * @param reason the reason for declining
     * @return the updated quote request
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if not in PENDING state
     */
    QuoteRequestResponse declineRequest(Long requestId, String reason);

    /**
     * Cancels a quote request by its owner.
     *
     * @param requestId the quote request ID
     * @return the updated quote request
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if not in PENDING state or not the owner
     */
    QuoteRequestResponse cancelRequest(Long requestId);

    /**
     * Expires all pending quote requests that have passed their expiration date.
     * Intended to be called by a scheduled task.
     */
    void expireRequests();
}
