package com.nifilili.order.mapper;

import com.nifilili.core.entity.BaseEntity;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.RefundStatus;
import com.nifilili.core.enums.order.ReturnStatus;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderRefundEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import com.nifilili.order.dto.response.OrderItemDetailsResponse;
import com.nifilili.order.dto.response.OrderSummaryResponse;
import com.nifilili.order.dto.response.RefundResponse;
import com.nifilili.order.dto.response.ReturnResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderMappersTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
    private final CouponMapper couponMapper = Mappers.getMapper(CouponMapper.class);
    private final ReturnMapper returnMapper = Mappers.getMapper(ReturnMapper.class);
    private final RefundMapper refundMapper = Mappers.getMapper(RefundMapper.class);

    @Test
    void allMappers_WhenMappingsAreUsed_ShouldMapExpectedFields() throws Exception {
        // OrderMapper.toSummary — maps id → orderId, enum → string
        OrderEntity order = OrderEntity.builder()
                .orderNumber("ORD-ABC12345")
                .status(OrderStatus.PLACED)
                .totalAmount(new BigDecimal("1500.00"))
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();
        setId(order, 42L);

        OrderSummaryResponse summary = orderMapper.toSummary(order);
        assertEquals(42L, summary.getOrderId());
        assertEquals("ORD-ABC12345", summary.getOrderNumber());
        assertEquals("PLACED", summary.getStatus());
        assertEquals(new BigDecimal("1500.00"), summary.getTotalAmount());

        // OrderMapper.toItemDetails — maps id → orderItemId, ignores timeline
        OrderItemEntity item = OrderItemEntity.builder()
                .businessId(5L)
                .title("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .status(OrderItemStatus.PREPARING)
                .build();
        setId(item, 99L);

        OrderItemDetailsResponse itemDetails = orderMapper.toItemDetails(item);
        assertEquals(99L, itemDetails.getOrderItemId());
        assertEquals(5L, itemDetails.getBusinessId());
        assertEquals("Test Product", itemDetails.getTitle());
        assertEquals(2, itemDetails.getQuantity());
        assertEquals("PREPARING", itemDetails.getStatus());
        assertNull(itemDetails.getTimeline()); // intentionally ignored by mapper

        // CouponMapper.toEntity — ignores businessId, sets isActive=true
        CreateCouponRequest couponReq = new CreateCouponRequest();
        couponReq.setCode("SAVE10");
        couponReq.setDiscountType("PERCENTAGE");
        couponReq.setDiscountValue(new BigDecimal("10"));
        couponReq.setMaxDiscount(new BigDecimal("500"));
        couponReq.setValidFrom(LocalDate.of(2024, 1, 1));
        couponReq.setValidTo(LocalDate.of(2024, 12, 31));

        CouponEntity coupon = couponMapper.toEntity(couponReq);
        assertEquals("SAVE10", coupon.getCode());
        assertEquals(new BigDecimal("10"), coupon.getDiscountValue());
        assertTrue(coupon.getIsActive()); // constant set by mapper
        assertNull(coupon.getBusinessId()); // intentionally ignored

        // CouponMapper.toResponse
        coupon.setBusinessId(1L);
        setId(coupon, 77L);
        CouponResponse couponResp = couponMapper.toResponse(coupon);
        assertEquals(77L, couponResp.getId());
        assertEquals("SAVE10", couponResp.getCode());
        assertNotNull(couponResp.getDiscountType());

        // ReturnMapper.toResponse — maps id → returnRequestId
        ReturnRequestEntity returnReq = ReturnRequestEntity.builder()
                .rmaNumber("RMA-XYZ12345")
                .status(ReturnStatus.REQUESTED)
                .build();
        setId(returnReq, 55L);

        ReturnResponse returnResp = returnMapper.toResponse(returnReq);
        assertEquals(55L, returnResp.getReturnRequestId());
        assertEquals("RMA-XYZ12345", returnResp.getRmaNumber());
        assertEquals("REQUESTED", returnResp.getStatus());

        // RefundMapper.toResponse — maps id → refundId
        OrderRefundEntity refund = OrderRefundEntity.builder()
                .refundAmount(new BigDecimal("250.00"))
                .status(RefundStatus.PENDING)
                .build();
        setId(refund, 33L);

        RefundResponse refundResp = refundMapper.toResponse(refund);
        assertEquals(33L, refundResp.getRefundId());
        assertEquals(new BigDecimal("250.00"), refundResp.getRefundAmount());
        assertEquals("PENDING", refundResp.getStatus());
    }

    private void setId(BaseEntity entity, Long id) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
