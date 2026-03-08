package com.nifilili.quote.service;

import com.nifilili.quote.dto.request.CreateQuoteRequest;
import com.nifilili.quote.dto.request.RejectQuoteRequest;
import com.nifilili.quote.dto.response.QuoteResponse;

import java.util.List;

public interface QuoteService {

    /**
     * Creates a draft quote for a quote request.
     *
     * @param requestId the quote request ID
     * @param request the quote details including line items
     * @return the created draft quote
     * @throws com.nifilili.core.exception.ResourceNotFoundException if request not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if request is terminal or draft already exists
     */
    QuoteResponse createDraft(Long requestId, CreateQuoteRequest request);

    /**
     * Sends a draft quote to the customer.
     *
     * @param quoteId the quote ID
     * @return the updated quote
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if not in DRAFT state
     */
    QuoteResponse sendQuote(Long quoteId);

    /**
     * Creates a revised version of a sent quote, marking the old one as REVISED.
     *
     * @param quoteId the original quote ID
     * @param request the revised quote details
     * @return the new draft quote
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if not in SENT state
     */
    QuoteResponse reviseQuote(Long quoteId, CreateQuoteRequest request);

    /**
     * Accepts a sent quote on behalf of the authenticated customer.
     * Publishes a QuoteAcceptedEvent to trigger order creation.
     *
     * @param quoteId the quote ID
     * @return the updated quote
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if not SENT, expired, or not the request owner
     */
    QuoteResponse acceptQuote(Long quoteId);

    /**
     * Rejects a sent quote on behalf of the authenticated customer.
     *
     * @param quoteId the quote ID
     * @param request the rejection reason
     * @return the updated quote
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     * @throws com.nifilili.core.exception.InvalidQuoteStateException if not SENT or not the request owner
     */
    QuoteResponse rejectQuote(Long quoteId, RejectQuoteRequest request);

    /**
     * Retrieves a quote by ID with its line items.
     *
     * @param quoteId the quote ID
     * @return the quote with line items
     * @throws com.nifilili.core.exception.ResourceNotFoundException if not found
     */
    QuoteResponse getQuote(Long quoteId);

    /**
     * Lists all quotes (revisions) for a quote request.
     *
     * @param requestId the quote request ID
     * @return list of quotes ordered by most recent first
     */
    List<QuoteResponse> listQuotesForRequest(Long requestId);

    /**
     * Gets the most recent quote for a quote request.
     *
     * @param requestId the quote request ID
     * @return the latest quote
     * @throws com.nifilili.core.exception.ResourceNotFoundException if no quotes exist
     */
    QuoteResponse getLatestQuote(Long requestId);

    /**
     * Expires all sent quotes that have passed their validity date.
     * Intended to be called by a scheduled task.
     */
    void expireQuotes();
}
