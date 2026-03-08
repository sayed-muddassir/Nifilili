package com.nifilili.quote.repository;

import com.nifilili.quote.domain.QuoteConversionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuoteConversionRepository extends JpaRepository<QuoteConversionEntity, Long> {

    Optional<QuoteConversionEntity> findByQuoteId(Long quoteId);

    boolean existsByQuoteId(Long quoteId);
}
