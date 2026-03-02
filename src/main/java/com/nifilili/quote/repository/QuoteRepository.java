package com.nifilili.quote.repository;

import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.quote.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findTopByRequestIdAndStatusInOrderByIdDesc(
            Long requestId,
            List<QuoteStatus> statuses
    );

    List<Quote> findByRequestIdOrderByIdDesc(Long requestId);

    Optional<Quote> findTopByRequestIdOrderByIdDesc(Long requestId);

    List<Quote> findByStatusAndValidUntilBefore(
            QuoteStatus status,
            java.time.LocalDateTime validUntil
    );
}
