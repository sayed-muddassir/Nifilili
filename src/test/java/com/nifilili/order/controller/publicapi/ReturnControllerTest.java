package com.nifilili.order.controller.publicapi;

import com.nifilili.order.dto.request.CreateReturnRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.service.ReturnService;
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
class ReturnControllerTest {

    @Mock
    private ReturnService returnService;

    @InjectMocks
    private ReturnController returnController;

    @Test
    void createReturn_WhenServiceReturnsResponse_ShouldDelegateAndReturnCreated() {
        CreateReturnRequest request = new CreateReturnRequest();
        ReturnResponse response = ReturnResponse.builder().build();
        when(returnService.createReturn(1L, request)).thenReturn(response);

        ResponseEntity<ReturnResponse> result = returnController.createReturn(1L, request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
    }
}
