package com.nifilili.quote.repository;

import com.nifilili.quote.domain.QuoteLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteLineItemRepository
        extends JpaRepository<QuoteLineItem, Long> {

    List<QuoteLineItem> findByQuoteId(Long quoteId);

    void deleteByQuoteId(Long quoteId);
}
