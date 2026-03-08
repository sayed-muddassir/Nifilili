package com.nifilili.quote.repository;

import com.nifilili.quote.domain.QuoteLineItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteLineItemRepository extends JpaRepository<QuoteLineItemEntity, Long> {

    List<QuoteLineItemEntity> findByQuoteIdOrderByIdAsc(Long quoteId);

    void deleteByQuoteId(Long quoteId);
}
