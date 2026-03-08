package com.nifilili.quote.validation;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.core.exception.InvalidQuoteStateException;
import com.nifilili.quote.domain.QuoteEntity;
import com.nifilili.quote.domain.QuoteRequestEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteValidatorTest {

    private static final Long ID = 1L;

    // --- ensureRequestNotTerminal ---

    @Test
    void ensureRequestNotTerminal_WhenPending_ShouldNotThrow() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.PENDING);
        assertThatCode(() -> QuoteValidator.ensureRequestNotTerminal(request))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureRequestNotTerminal_WhenQuoted_ShouldNotThrow() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.QUOTED);
        assertThatCode(() -> QuoteValidator.ensureRequestNotTerminal(request))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = QuoteRequestStatus.class, names = {"ACCEPTED", "REJECTED", "EXPIRED", "CANCELLED"})
    void ensureRequestNotTerminal_WhenTerminal_ShouldThrow(QuoteRequestStatus status) {
        QuoteRequestEntity request = buildRequest(status);
        assertThatThrownBy(() -> QuoteValidator.ensureRequestNotTerminal(request))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("terminal state")
                .hasMessageContaining(status.name());
    }

    // --- ensureQuoteNotExpired ---

    @Test
    void ensureQuoteNotExpired_WhenValid_ShouldNotThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        quote.setValidUntil(LocalDateTime.now().plusDays(7));
        assertThatCode(() -> QuoteValidator.ensureQuoteNotExpired(quote))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureQuoteNotExpired_WhenExpired_ShouldThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        quote.setValidUntil(LocalDateTime.now().minusDays(1));
        assertThatThrownBy(() -> QuoteValidator.ensureQuoteNotExpired(quote))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("expired");
    }

    // --- ensureQuoteStatus ---

    @Test
    void ensureQuoteStatus_WhenMatches_ShouldNotThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.DRAFT);
        assertThatCode(() -> QuoteValidator.ensureQuoteStatus(quote, QuoteStatus.DRAFT))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureQuoteStatus_WhenMismatch_ShouldThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.DRAFT);
        assertThatThrownBy(() -> QuoteValidator.ensureQuoteStatus(quote, QuoteStatus.SENT))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("expected SENT");
    }

    // --- ensureRequestStatus ---

    @Test
    void ensureRequestStatus_WhenMatches_ShouldNotThrow() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.PENDING);
        assertThatCode(() -> QuoteValidator.ensureRequestStatus(request, QuoteRequestStatus.PENDING))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureRequestStatus_WhenMismatch_ShouldThrow() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.PENDING);
        assertThatThrownBy(() -> QuoteValidator.ensureRequestStatus(request, QuoteRequestStatus.QUOTED))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("expected QUOTED");
    }

    private QuoteRequestEntity buildRequest(QuoteRequestStatus status) {
        QuoteRequestEntity entity = QuoteRequestEntity.builder()
                .requestNumber("QREQ-TEST1234")
                .status(status)
                .build();
        return TestEntityIdUtil.withId(entity, ID);
    }

    private QuoteEntity buildQuote(QuoteStatus status) {
        QuoteEntity entity = QuoteEntity.builder()
                .quoteNumber("QTE-TEST1234")
                .status(status)
                .validUntil(LocalDateTime.now().plusDays(14))
                .build();
        return TestEntityIdUtil.withId(entity, ID);
    }
}
