package com.nifilili.quote.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.core.exception.InvalidQuoteStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.quote.domain.QuoteRequestEntity;
import com.nifilili.quote.dto.request.CreateQuoteRequestRequest;
import com.nifilili.quote.dto.response.QuoteRequestResponse;
import com.nifilili.quote.events.QuoteRequestCreatedEvent;
import com.nifilili.quote.events.QuoteRequestDeclinedEvent;
import com.nifilili.quote.mapper.QuoteRequestMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteRequestServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long BUSINESS_ID = 100L;
    private static final Long OFFERING_ID = 200L;
    private static final Long REQUEST_ID = 1L;

    @Mock
    private QuoteRequestRepository quoteRequestRepository;

    @Mock
    private QuoteRequestMapper quoteRequestMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuoteRequestServiceImpl quoteRequestService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void createRequest_WhenValidInput_ShouldCreateAndPublishEvent() {
        CreateQuoteRequestRequest request = new CreateQuoteRequestRequest();
        request.setOfferingId(OFFERING_ID);
        request.setBusinessId(BUSINESS_ID);
        request.setRequirements("Build a custom website");

        when(quoteRequestRepository.save(any(QuoteRequestEntity.class)))
                .thenAnswer(inv -> {
                    QuoteRequestEntity entity = inv.getArgument(0);
                    return TestEntityIdUtil.withId(entity, REQUEST_ID);
                });
        when(quoteRequestMapper.toResponse(any(QuoteRequestEntity.class)))
                .thenReturn(QuoteRequestResponse.builder()
                        .requestId(REQUEST_ID)
                        .status(QuoteRequestStatus.PENDING.name())
                        .build());

        QuoteRequestResponse response = quoteRequestService.createRequest(request);

        assertThat(response.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(response.getStatus()).isEqualTo("PENDING");

        ArgumentCaptor<QuoteRequestEntity> captor = ArgumentCaptor.forClass(QuoteRequestEntity.class);
        verify(quoteRequestRepository).save(captor.capture());
        QuoteRequestEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(QuoteRequestStatus.PENDING);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getBusinessId()).isEqualTo(BUSINESS_ID);
        assertThat(saved.getRequestNumber()).startsWith("QREQ-");

        verify(eventPublisher).publishEvent(any(QuoteRequestCreatedEvent.class));
    }

    @Test
    void listMyRequests_WhenCalled_ShouldReturnUserRequests() {
        QuoteRequestEntity entity = buildRequest(QuoteRequestStatus.PENDING);
        when(quoteRequestRepository.findByUserIdOrderByIdDesc(USER_ID))
                .thenReturn(List.of(entity));
        when(quoteRequestMapper.toResponse(entity))
                .thenReturn(QuoteRequestResponse.builder().requestId(REQUEST_ID).build());

        List<QuoteRequestResponse> result = quoteRequestService.listMyRequests();

        assertThat(result).hasSize(1);
        verify(quoteRequestRepository).findByUserIdOrderByIdDesc(USER_ID);
    }

    @Test
    void declineRequest_WhenPending_ShouldRejectAndPublishEvent() {
        QuoteRequestEntity entity = buildRequest(QuoteRequestStatus.PENDING);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));
        when(quoteRequestRepository.save(any(QuoteRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteRequestMapper.toResponse(any(QuoteRequestEntity.class)))
                .thenReturn(QuoteRequestResponse.builder()
                        .requestId(REQUEST_ID)
                        .status(QuoteRequestStatus.REJECTED.name())
                        .build());

        QuoteRequestResponse response = quoteRequestService.declineRequest(REQUEST_ID, "Not feasible");

        assertThat(response.getStatus()).isEqualTo("REJECTED");

        ArgumentCaptor<QuoteRequestEntity> captor = ArgumentCaptor.forClass(QuoteRequestEntity.class);
        verify(quoteRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuoteRequestStatus.REJECTED);
        assertThat(captor.getValue().getRejectionReason()).isEqualTo("Not feasible");

        verify(eventPublisher).publishEvent(any(QuoteRequestDeclinedEvent.class));
    }

    @Test
    void declineRequest_WhenNotPending_ShouldThrowInvalidState() {
        QuoteRequestEntity entity = buildRequest(QuoteRequestStatus.ACCEPTED);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> quoteRequestService.declineRequest(REQUEST_ID, "reason"))
                .isInstanceOf(InvalidQuoteStateException.class);

        verify(quoteRequestRepository, never()).save(any());
    }

    @Test
    void cancelRequest_WhenPendingAndOwner_ShouldCancel() {
        QuoteRequestEntity entity = buildRequest(QuoteRequestStatus.PENDING);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));
        when(quoteRequestRepository.save(any(QuoteRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(quoteRequestMapper.toResponse(any(QuoteRequestEntity.class)))
                .thenReturn(QuoteRequestResponse.builder()
                        .requestId(REQUEST_ID)
                        .status(QuoteRequestStatus.CANCELLED.name())
                        .build());

        QuoteRequestResponse response = quoteRequestService.cancelRequest(REQUEST_ID);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelRequest_WhenNotOwner_ShouldThrow() {
        QuoteRequestEntity entity = buildRequest(QuoteRequestStatus.PENDING);
        entity.setUserId(999L);
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> quoteRequestService.cancelRequest(REQUEST_ID))
                .isInstanceOf(InvalidQuoteStateException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void getRequest_WhenNotFound_ShouldThrow() {
        when(quoteRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteRequestService.getRequest(REQUEST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void expireRequests_WhenExpired_ShouldUpdateStatus() {
        QuoteRequestEntity entity = buildRequest(QuoteRequestStatus.PENDING);
        when(quoteRequestRepository.findByStatusAndExpiredAtBefore(
                any(QuoteRequestStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(entity));
        when(quoteRequestRepository.save(any(QuoteRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        quoteRequestService.expireRequests();

        ArgumentCaptor<QuoteRequestEntity> captor = ArgumentCaptor.forClass(QuoteRequestEntity.class);
        verify(quoteRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuoteRequestStatus.EXPIRED);
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
}
