package com.nifilili.quote.service.impl;

import com.nifilili.quote.domain.QuoteConversionEntity;
import com.nifilili.quote.dto.response.QuoteConversionResponse;
import com.nifilili.quote.repository.QuoteConversionRepository;
import com.nifilili.quote.service.QuoteConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteConversionServiceImpl implements QuoteConversionService {

    private final QuoteConversionRepository quoteConversionRepository;

    @Override
    @Transactional
    public void recordConversion(Long quoteId, Long orderId, Long convertedBy) {
        if (quoteConversionRepository.existsByQuoteId(quoteId)) {
            log.debug("Conversion already recorded for quoteId={}, skipping", quoteId);
            return;
        }

        log.info("Recording quote conversion: quoteId={}, orderId={}", quoteId, orderId);
        QuoteConversionEntity entity = QuoteConversionEntity.builder()
                .quoteId(quoteId)
                .orderId(orderId)
                .convertedAt(LocalDateTime.now())
                .convertedBy(convertedBy)
                .build();
        quoteConversionRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuoteConversionResponse> getConversion(Long quoteId) {
        log.debug("Getting conversion for quoteId={}", quoteId);
        return quoteConversionRepository.findByQuoteId(quoteId)
                .map(entity -> QuoteConversionResponse.builder()
                        .conversionId(entity.getId())
                        .quoteId(entity.getQuoteId())
                        .orderId(entity.getOrderId())
                        .convertedAt(entity.getConvertedAt())
                        .convertedBy(entity.getConvertedBy())
                        .build());
    }
}
