package com.nifilili.order.controller.owner;

import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import com.nifilili.order.service.CouponService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessCouponControllerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private BusinessCouponController businessCouponController;

    @Test
    void allCouponEndpoints_WhenServiceReturnsResponses_ShouldDelegateAndReturnCorrectStatus() {
        CreateCouponRequest createReq = new CreateCouponRequest();
        CouponResponse couponResp = CouponResponse.builder().build();
        when(couponService.createCoupon(createReq)).thenReturn(couponResp);
        when(couponService.updateCoupon(1L, createReq)).thenReturn(couponResp);

        Page<CouponResponse> page = new PageImpl<>(List.of());
        Pageable pageable = Pageable.unpaged();
        when(couponService.listCoupons(pageable)).thenReturn(page);

        ResponseEntity<CouponResponse> createResult = businessCouponController.createCoupon(createReq);
        assertEquals(HttpStatus.CREATED, createResult.getStatusCode());
        assertSame(couponResp, createResult.getBody());

        ResponseEntity<CouponResponse> updateResult = businessCouponController.updateCoupon(1L, createReq);
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertSame(couponResp, updateResult.getBody());

        ResponseEntity<Page<CouponResponse>> listResult = businessCouponController.listCoupons(pageable);
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(page, listResult.getBody());

        ResponseEntity<Void> deactivateResult = businessCouponController.deactivate(1L);
        assertEquals(HttpStatus.NO_CONTENT, deactivateResult.getStatusCode());
        verify(couponService).deactivate(1L);
    }
}
