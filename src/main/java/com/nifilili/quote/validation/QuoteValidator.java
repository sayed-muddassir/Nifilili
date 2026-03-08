package com.nifilili.quote.validation;

import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.core.exception.InvalidQuoteStateException;
import com.nifilili.quote.domain.QuoteEntity;
import com.nifilili.quote.domain.QuoteRequestEntity;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.Set;

@UtilityClass
public class QuoteValidator {

    private static final Set<QuoteRequestStatus> TERMINAL_REQUEST_STATUSES = Set.of(
            QuoteRequestStatus.ACCEPTED,
            QuoteRequestStatus.REJECTED,
            QuoteRequestStatus.EXPIRED,
            QuoteRequestStatus.CANCELLED
    );

    public static void ensureRequestNotTerminal(QuoteRequestEntity request) {
        if (TERMINAL_REQUEST_STATUSES.contains(request.getStatus())) {
            throw new InvalidQuoteStateException(
                    "Quote request " + request.getRequestNumber() + " is in terminal state: " + request.getStatus());
        }
    }

    public static void ensureQuoteNotExpired(QuoteEntity quote) {
        if (quote.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new InvalidQuoteStateException(
                    "Quote " + quote.getQuoteNumber() + " has expired on " + quote.getValidUntil());
        }
    }

    public static void ensureQuoteStatus(QuoteEntity quote, QuoteStatus expected) {
        if (quote.getStatus() != expected) {
            throw new InvalidQuoteStateException(
                    "Quote " + quote.getQuoteNumber() + " is in state " + quote.getStatus()
                            + ", expected " + expected);
        }
    }

    public static void ensureRequestStatus(QuoteRequestEntity request, QuoteRequestStatus expected) {
        if (request.getStatus() != expected) {
            throw new InvalidQuoteStateException(
                    "Quote request " + request.getRequestNumber() + " is in state " + request.getStatus()
                            + ", expected " + expected);
        }
    }
}
