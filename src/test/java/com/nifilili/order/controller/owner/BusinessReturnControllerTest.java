package com.nifilili.order.controller.owner;

import com.nifilili.order.dto.request.UpdateReturnStatusRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.service.BusinessReturnService;
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
class BusinessReturnControllerTest {

    @Mock
    private BusinessReturnService businessReturnService;

    @InjectMocks
    private BusinessReturnController businessReturnController;

    @Test
    void updateStatus_WhenServiceReturnsResponse_ShouldDelegateAndReturnOk() {
        UpdateReturnStatusRequest request = new UpdateReturnStatusRequest();
        ReturnResponse response = ReturnResponse.builder().build();
        when(businessReturnService.updateStatus(1L, request)).thenReturn(response);

        ResponseEntity<ReturnResponse> result = businessReturnController.updateStatus(1L, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
    }
}
