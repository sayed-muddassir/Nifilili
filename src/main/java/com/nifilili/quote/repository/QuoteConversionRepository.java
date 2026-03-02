package com.nifilili.quote.repository;

import com.nifilili.quote.domain.QuoteConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuoteConversionRepository
        extends JpaRepository<QuoteConversion, Long> {

    boolean existsByQuoteId(Long quoteId);

    Optional<QuoteConversion> findByQuoteId(Long quoteId);
}
