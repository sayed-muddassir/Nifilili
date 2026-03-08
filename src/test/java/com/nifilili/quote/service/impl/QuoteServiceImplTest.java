package com.nifilili.quote.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.core.exception.InvalidQuoteStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.quote.domain.QuoteEntity;
import com.nifilili.quote.domain.QuoteLineItemEntity;
import com.nifilili.quote.domain.QuoteRequestEntity;
import com.nifilili.quote.dto.request.CreateQuoteRequest;
import com.nifilili.quote.dto.request.QuoteLineItemRequest;
import com.nifilili.quote.dto.request.RejectQuoteRequest;
import com.nifilili.quote.dto.response.QuoteLineItemResponse;
import com.nifilili.quote.dto.response.QuoteResponse;
import com.nifilili.quote.events.QuoteAcceptedEvent;
import com.nifilili.quote.events.QuoteRejectedEvent;
import com.nifilili.quote.events.QuoteSentEvent;
import com.nifilili.quote.mapper.QuoteMapper;
import com.nifilili.quote.repository.QuoteLineItemRepository;
import com.nifilili.quote.repository.QuoteRepository;
import com.nifilili.quote.repository.QuoteRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long BUSINESS_ID = 100L;
    private static final Long OFFERING_ID = 200L;
    private static final Long REQUEST_ID = 1L;
    private static final Long QUOTE_ID = 10L;
    private static final Long LINE_ITEM_ID = 50L;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteRequestRepository quoteRequestRepository;

    @Mock
    private QuoteLineItemRepository quoteLineItemRepository;

    @Mock
    private QuoteMapper quoteMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void createDraft_WhenValidRequest_ShouldCreateDraftWithLineItems() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.PENDING);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(quoteRepository.existsByRequestIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
        when(quoteRepository.save(any(QuoteEntity.class))).thenAnswer(inv -> {
            QuoteEntity q = inv.getArgument(0);
            return TestEntityIdUtil.withId(q, QUOTE_ID);
        });
        when(quoteLineItemRepository.save(any(QuoteLineItemEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(quoteLineItemRepository.findByQuoteIdOrderByIdAsc(QUOTE_ID))
                .thenReturn(Collections.emptyList());
        when(quoteMapper.toResponse(any(QuoteEntity.class)))
                .thenReturn(QuoteResponse.builder()
                        .quoteId(QUOTE_ID)
                        .status(QuoteStatus.DRAFT.name())
                        .build());

        QuoteResponse response = quoteService.createDraft(REQUEST_ID, buildCreateQuoteRequest());

        assertThat(response.getQuoteId()).isEqualTo(QUOTE_ID);
        assertThat(response.getStatus()).isEqualTo("DRAFT");

        ArgumentCaptor<QuoteEntity> captor = ArgumentCaptor.forClass(QuoteEntity.class);
        verify(quoteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuoteStatus.DRAFT);
        assertThat(captor.getValue().getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(captor.getValue().getQuoteNumber()).startsWith("QTE-");

        verify(quoteLineItemRepository).save(any(QuoteLineItemEntity.class));
    }

    @Test
    void createDraft_WhenRequestTerminal_ShouldThrow() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.ACCEPTED);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> quoteService.createDraft(REQUEST_ID, buildCreateQuoteRequest()))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("terminal");

        verify(quoteRepository, never()).save(any());
    }

    @Test
    void createDraft_WhenDraftAlreadyExists_ShouldThrow() {
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.PENDING);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(quoteRepository.existsByRequestIdAndStatusIn(anyLong(), anyList())).thenReturn(true);

        assertThatThrownBy(() -> quoteService.createDraft(REQUEST_ID, buildCreateQuoteRequest()))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("active quote");
    }

    @Test
    void sendQuote_WhenDraft_ShouldTransitionToSent() {
        QuoteEntity quote = buildQuote(QuoteStatus.DRAFT);
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.PENDING);

        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(QuoteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(quoteRequestRepository.save(any(QuoteRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteLineItemRepository.findByQuoteIdOrderByIdAsc(QUOTE_ID))
                .thenReturn(Collections.emptyList());
        when(quoteMapper.toResponse(any(QuoteEntity.class)))
                .thenReturn(QuoteResponse.builder()
                        .quoteId(QUOTE_ID)
                        .status(QuoteStatus.SENT.name())
                        .build());

        QuoteResponse response = quoteService.sendQuote(QUOTE_ID);

        assertThat(response.getStatus()).isEqualTo("SENT");

        ArgumentCaptor<QuoteEntity> captor = ArgumentCaptor.forClass(QuoteEntity.class);
        verify(quoteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuoteStatus.SENT);
        assertThat(captor.getValue().getSentAt()).isNotNull();

        verify(eventPublisher).publishEvent(any(QuoteSentEvent.class));
    }

    @Test
    void sendQuote_WhenNotDraft_ShouldThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> quoteService.sendQuote(QUOTE_ID))
                .isInstanceOf(InvalidQuoteStateException.class);
    }

    @Test
    void reviseQuote_WhenSent_ShouldMarkRevisedAndCreateNew() {
        QuoteEntity oldQuote = buildQuote(QuoteStatus.SENT);
        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(oldQuote));
        when(quoteRepository.save(any(QuoteEntity.class))).thenAnswer(inv -> {
            QuoteEntity q = inv.getArgument(0);
            if (q.getId() == null) {
                return TestEntityIdUtil.withId(q, QUOTE_ID + 1);
            }
            return q;
        });
        when(quoteLineItemRepository.save(any(QuoteLineItemEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(quoteLineItemRepository.findByQuoteIdOrderByIdAsc(anyLong()))
                .thenReturn(Collections.emptyList());
        when(quoteMapper.toResponse(any(QuoteEntity.class)))
                .thenReturn(QuoteResponse.builder()
                        .quoteId(QUOTE_ID + 1)
                        .status(QuoteStatus.DRAFT.name())
                        .build());

        QuoteResponse response = quoteService.reviseQuote(QUOTE_ID, buildCreateQuoteRequest());

        assertThat(response.getQuoteId()).isEqualTo(QUOTE_ID + 1);

        // Verify both saves: first = old quote REVISED, second = new DRAFT
        ArgumentCaptor<QuoteEntity> captor = ArgumentCaptor.forClass(QuoteEntity.class);
        verify(quoteRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getStatus()).isEqualTo(QuoteStatus.REVISED);
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(QuoteStatus.DRAFT);
    }

    @Test
    void reviseQuote_WhenNotSent_ShouldThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.DRAFT);
        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> quoteService.reviseQuote(QUOTE_ID, buildCreateQuoteRequest()))
                .isInstanceOf(InvalidQuoteStateException.class);
    }

    @Test
    void acceptQuote_WhenSentAndValid_ShouldAcceptAndPublishEvent() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        quote.setValidUntil(LocalDateTime.now().plusDays(7));
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.QUOTED);

        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(QuoteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(quoteRequestRepository.save(any(QuoteRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteLineItemRepository.findByQuoteIdOrderByIdAsc(QUOTE_ID))
                .thenReturn(List.of(buildLineItem()));
        when(quoteMapper.toResponse(any(QuoteEntity.class)))
                .thenReturn(QuoteResponse.builder()
                        .quoteId(QUOTE_ID)
                        .status(QuoteStatus.ACCEPTED.name())
                        .build());

        QuoteResponse response = quoteService.acceptQuote(QUOTE_ID);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");

        ArgumentCaptor<QuoteEntity> captor = ArgumentCaptor.forClass(QuoteEntity.class);
        verify(quoteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuoteStatus.ACCEPTED);
        assertThat(captor.getValue().getAcceptedAt()).isNotNull();

        verify(eventPublisher).publishEvent(any(QuoteAcceptedEvent.class));
    }

    @Test
    void acceptQuote_WhenExpired_ShouldThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        quote.setValidUntil(LocalDateTime.now().minusDays(1));
//        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.QUOTED);

        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
//        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> quoteService.acceptQuote(QUOTE_ID))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("expired");

        verify(eventPublisher, never()).publishEvent(any(QuoteAcceptedEvent.class));
    }

    @Test
    void acceptQuote_WhenNotOwner_ShouldThrow() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        quote.setValidUntil(LocalDateTime.now().plusDays(7));
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.QUOTED);
        request.setUserId(999L);

        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> quoteService.acceptQuote(QUOTE_ID))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void rejectQuote_WhenSent_ShouldReject() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        QuoteRequestEntity request = buildRequest(QuoteRequestStatus.QUOTED);

        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(QuoteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(quoteRequestRepository.save(any(QuoteRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteLineItemRepository.findByQuoteIdOrderByIdAsc(QUOTE_ID))
                .thenReturn(Collections.emptyList());
        when(quoteMapper.toResponse(any(QuoteEntity.class)))
                .thenReturn(QuoteResponse.builder()
                        .quoteId(QUOTE_ID)
                        .status(QuoteStatus.REJECTED.name())
                        .build());

        RejectQuoteRequest rejectRequest = new RejectQuoteRequest();
        rejectRequest.setReason("Too expensive");

        QuoteResponse response = quoteService.rejectQuote(QUOTE_ID, rejectRequest);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        verify(eventPublisher).publishEvent(any(QuoteRejectedEvent.class));
    }

    @Test
    void getQuote_WhenNotFound_ShouldThrow() {
        when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.getQuote(QUOTE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLatestQuote_WhenNoQuotes_ShouldThrow() {
        when(quoteRepository.findFirstByRequestIdOrderByIdDesc(REQUEST_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.getLatestQuote(REQUEST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void expireQuotes_WhenPastValidUntil_ShouldExpire() {
        QuoteEntity quote = buildQuote(QuoteStatus.SENT);
        when(quoteRepository.findByStatusAndValidUntilBefore(
                any(QuoteStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(quote));
        when(quoteRepository.save(any(QuoteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(buildRequest(QuoteRequestStatus.QUOTED)));

        quoteService.expireQuotes();

        ArgumentCaptor<QuoteEntity> captor = ArgumentCaptor.forClass(QuoteEntity.class);
        verify(quoteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuoteStatus.EXPIRED);
    }

    private QuoteRequestEntity buildRequest(QuoteRequestStatus status) {
        QuoteRequestEntity entity = QuoteRequestEntity.builder()
                .offeringId(OFFERING_ID)
                .userId(USER_ID)
                .businessId(BUSINESS_ID)
                .requestNumber("QREQ-TEST1234")
                .requirements("Test requirements")
                .status(status)
                .submittedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(USER_ID)
                .updatedBy(USER_ID)
                .build();
        return TestEntityIdUtil.withId(entity, REQUEST_ID);
    }

    private QuoteEntity buildQuote(QuoteStatus status) {
        QuoteEntity entity = QuoteEntity.builder()
                .requestId(REQUEST_ID)
                .quoteNumber("QTE-TEST1234")
                .serviceDetails("Website development")
                .totalAmount(new BigDecimal("1500.00"))
                .currency("NPR")
                .estimatedDurationDays(30)
                .validUntil(LocalDateTime.now().plusDays(14))
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(USER_ID)
                .updatedBy(USER_ID)
                .build();
        return TestEntityIdUtil.withId(entity, QUOTE_ID);
    }

    private QuoteLineItemEntity buildLineItem() {
        QuoteLineItemEntity entity = QuoteLineItemEntity.builder()
                .quoteId(QUOTE_ID)
                .description("Design phase")
                .quantity(1)
                .unitPrice(new BigDecimal("500.00"))
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(USER_ID)
                .updatedBy(USER_ID)
                .build();
        return TestEntityIdUtil.withId(entity, LINE_ITEM_ID);
    }

    private CreateQuoteRequest buildCreateQuoteRequest() {
        QuoteLineItemRequest lineItem = new QuoteLineItemRequest();
        lineItem.setDescription("Design phase");
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(new BigDecimal("500.00"));

        CreateQuoteRequest request = new CreateQuoteRequest();
        request.setServiceDetails("Website development");
        request.setCurrency("NPR");
        request.setEstimatedDurationDays(30);
        request.setValidityDays(14);
        request.setLineItems(List.of(lineItem));
        return request;
    }
}
