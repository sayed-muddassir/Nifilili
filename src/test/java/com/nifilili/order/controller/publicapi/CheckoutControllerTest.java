package com.nifilili.order.controller.publicapi;

import com.nifilili.order.dto.request.CheckoutPreviewRequest;
import com.nifilili.order.dto.response.CheckoutPreviewResponse;
import com.nifilili.order.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutController checkoutController;

    @Test
    void preview_WhenServiceReturnsResponse_ShouldDelegateAndReturnOk() {
        CheckoutPreviewRequest request = new CheckoutPreviewRequest();
        CheckoutPreviewResponse response = CheckoutPreviewResponse.builder().build();
        when(checkoutService.preview(request)).thenReturn(response);

        ResponseEntity<CheckoutPreviewResponse> result = checkoutController.preview(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
    }
}
