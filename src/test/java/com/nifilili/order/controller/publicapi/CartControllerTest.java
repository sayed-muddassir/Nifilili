package com.nifilili.order.controller.publicapi;

import com.nifilili.order.dto.request.AddCartItemRequest;
import com.nifilili.order.dto.request.UpdateCartItemRequest;
import com.nifilili.order.dto.response.CartResponse;
import com.nifilili.order.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    void allCartEndpoints_WhenServiceReturnsResponses_ShouldDelegateAndReturnCorrectStatus() {
        CartResponse cartResponse = CartResponse.builder().build();
        AddCartItemRequest addReq = new AddCartItemRequest();
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest();

        when(cartService.getCart()).thenReturn(cartResponse);
        when(cartService.addItem(addReq)).thenReturn(cartResponse);
        when(cartService.updateQuantity(1L, updateReq)).thenReturn(cartResponse);

        ResponseEntity<CartResponse> getResult = cartController.getCart();
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(cartResponse, getResult.getBody());

        ResponseEntity<CartResponse> addResult = cartController.addItem(addReq);
        assertEquals(HttpStatus.OK, addResult.getStatusCode());
        assertSame(cartResponse, addResult.getBody());

        ResponseEntity<CartResponse> updateResult = cartController.updateQuantity(1L, updateReq);
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertSame(cartResponse, updateResult.getBody());

        ResponseEntity<Void> removeResult = cartController.removeItem(1L);
        assertEquals(HttpStatus.NO_CONTENT, removeResult.getStatusCode());
        verify(cartService).removeItem(1L);

        ResponseEntity<Void> clearResult = cartController.clearCart();
        assertEquals(HttpStatus.NO_CONTENT, clearResult.getStatusCode());
        verify(cartService).clearCart();
    }
}
