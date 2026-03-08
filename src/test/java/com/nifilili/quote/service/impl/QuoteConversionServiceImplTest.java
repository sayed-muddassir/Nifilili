package com.nifilili.quote.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.quote.domain.QuoteConversionEntity;
import com.nifilili.quote.dto.response.QuoteConversionResponse;
import com.nifilili.quote.repository.QuoteConversionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteConversionServiceImplTest {

    private static final Long QUOTE_ID = 10L;
    private static final Long ORDER_ID = 20L;
    private static final Long USER_ID = 42L;
    private static final Long CONVERSION_ID = 1L;

    @Mock
    private QuoteConversionRepository quoteConversionRepository;

    @InjectMocks
    private QuoteConversionServiceImpl quoteConversionService;

    @Test
    void recordConversion_WhenNewConversion_ShouldSave() {
        when(quoteConversionRepository.existsByQuoteId(QUOTE_ID)).thenReturn(false);
        when(quoteConversionRepository.save(any(QuoteConversionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        quoteConversionService.recordConversion(QUOTE_ID, ORDER_ID, USER_ID);

        ArgumentCaptor<QuoteConversionEntity> captor = ArgumentCaptor.forClass(QuoteConversionEntity.class);
        verify(quoteConversionRepository).save(captor.capture());
        assertThat(captor.getValue().getQuoteId()).isEqualTo(QUOTE_ID);
        assertThat(captor.getValue().getOrderId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().getConvertedBy()).isEqualTo(USER_ID);
    }

    @Test
    void recordConversion_WhenAlreadyConverted_ShouldSkip() {
        when(quoteConversionRepository.existsByQuoteId(QUOTE_ID)).thenReturn(true);

        quoteConversionService.recordConversion(QUOTE_ID, ORDER_ID, USER_ID);

        verify(quoteConversionRepository, never()).save(any());
    }

    @Test
    void getConversion_WhenExists_ShouldReturnResponse() {
        QuoteConversionEntity entity = QuoteConversionEntity.builder()
                .quoteId(QUOTE_ID)
                .orderId(ORDER_ID)
                .convertedAt(LocalDateTime.now())
                .convertedBy(USER_ID)
                .build();
        TestEntityIdUtil.withId(entity, CONVERSION_ID);
        when(quoteConversionRepository.findByQuoteId(QUOTE_ID)).thenReturn(Optional.of(entity));

        Optional<QuoteConversionResponse> result = quoteConversionService.getConversion(QUOTE_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getQuoteId()).isEqualTo(QUOTE_ID);
        assertThat(result.get().getOrderId()).isEqualTo(ORDER_ID);
    }

    @Test
    void getConversion_WhenNotExists_ShouldReturnEmpty() {
        when(quoteConversionRepository.findByQuoteId(QUOTE_ID)).thenReturn(Optional.empty());

        Optional<QuoteConversionResponse> result = quoteConversionService.getConversion(QUOTE_ID);

        assertThat(result).isEmpty();
    }
}
