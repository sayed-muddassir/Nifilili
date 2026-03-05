package com.nifilili.order.controller.publicapi;

import com.nifilili.order.dto.request.CancellationRequest;
import com.nifilili.order.dto.response.CancellationResponse;
import com.nifilili.order.service.CancellationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancellationControllerTest {

    @Mock
    private CancellationService cancellationService;

    @InjectMocks
    private CancellationController cancellationController;

    @Test
    void requestCancellation_WhenServiceReturnsResponses_ShouldDelegateAndReturnCreated() {
        CancellationRequest request = new CancellationRequest();
        List<CancellationResponse> responses = List.of(CancellationResponse.builder().build());
        when(cancellationService.requestCancellation(1L, request)).thenReturn(responses);

        ResponseEntity<List<CancellationResponse>> result =
                cancellationController.requestCancellation(1L, request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(responses, result.getBody());
    }
}
