package com.nifilili.order.controller.publicapi;

import com.nifilili.order.dto.request.PlaceOrderRequest;
import com.nifilili.order.dto.response.OrderDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import com.nifilili.order.dto.response.PlaceOrderResponse;
import com.nifilili.order.service.OrderPlacementService;
import com.nifilili.order.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderPlacementService orderPlacementService;

    @Mock
    private OrderQueryService orderQueryService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void allOrderEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnCorrectStatus() {
        PlaceOrderRequest placeReq = new PlaceOrderRequest();
        PlaceOrderResponse placeResp = PlaceOrderResponse.builder().build();
        when(orderPlacementService.placeOrder(placeReq)).thenReturn(placeResp);

        Page<OrderSummaryResponse> page = new PageImpl<>(List.of());
        Pageable pageable = Pageable.unpaged();
        when(orderQueryService.listMyOrders(null, pageable)).thenReturn(page);

        OrderDetailsResponse detailsResp = OrderDetailsResponse.builder().build();
        when(orderQueryService.getOrderDetails(1L)).thenReturn(detailsResp);

        ResponseEntity<PlaceOrderResponse> placeResult = orderController.placeOrder(placeReq);
        assertEquals(HttpStatus.CREATED, placeResult.getStatusCode());
        assertSame(placeResp, placeResult.getBody());

        ResponseEntity<Page<OrderSummaryResponse>> listResult = orderController.listOrders(null, pageable);
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(page, listResult.getBody());

        ResponseEntity<OrderDetailsResponse> detailResult = orderController.getOrderDetails(1L);
        assertEquals(HttpStatus.OK, detailResult.getStatusCode());
        assertSame(detailsResp, detailResult.getBody());
    }
}
