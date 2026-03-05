package com.nifilili.order.domain;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.OrderStatus;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.enums.order.RefundStatus;
import com.nifilili.core.enums.order.ReturnStatus;
import com.nifilili.core.enums.util.DiscountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDomainModelTest {

    @Test
    void orderEntity_WhenBuilt_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        OrderEntity order = OrderEntity.builder()
                .userId(1L)
                .orderNumber("ORD-TEST1234")
                .invoiceNumber("INV-1234567890")
                .receiverName("John Doe")
                .contactNumber("9800000000")
                .email("john@example.com")
                .municipalityId(10L)
                .wardNumber(5)
                .toleName("Kathmandu")
                .addressField1("Thamel")
                .postalCode("44600")
                .subtotalAmount(new BigDecimal("1000.00"))
                .deliveryCharge(new BigDecimal("50.00"))
                .taxAmount(new BigDecimal("130.00"))
                .discountAmount(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("1080.00"))
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .amountPaid(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(1L)
                .updatedBy(1L)
                .build();

        assertEquals(1L, order.getUserId());
        assertEquals("ORD-TEST1234", order.getOrderNumber());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        assertFalse(order.getIsB2bOrder()); // @Builder.Default
    }

    @Test
    void orderItemEntity_WhenBuilt_ShouldSetAllFields() {
        OrderItemEntity item = OrderItemEntity.builder()
                .orderId(1L)
                .businessId(2L)
                .offeringId(3L)
                .variantId(4L)
                .title("Test Product")
                .sku("SKU-001")
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .subtotal(new BigDecimal("1000.00"))
                .discountAmount(BigDecimal.ZERO)
                .deliveryCharge(new BigDecimal("50.00"))
                .taxAmount(new BigDecimal("130.00"))
                .status(OrderItemStatus.PLACED)
                .variantAttributes(Map.of("color", "red"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedBy(1L)
                .build();

        assertEquals("Test Product", item.getTitle());
        assertEquals(2, item.getQuantity());
        assertEquals(OrderItemStatus.PLACED, item.getStatus());
        assertEquals("red", item.getVariantAttributes().get("color"));
    }

    @Test
    void couponEntity_WhenBuilt_ShouldSetAllFields() {
        CouponEntity coupon = CouponEntity.builder()
                .businessId(1L)
                .code("SAVE20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20"))
                .maxDiscount(new BigDecimal("500"))
                .validFrom(LocalDate.of(2024, 1, 1))
                .validTo(LocalDate.of(2024, 12, 31))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertEquals("SAVE20", coupon.getCode());
        assertEquals(DiscountType.PERCENTAGE, coupon.getDiscountType());
        assertTrue(coupon.getIsActive());
    }

    @Test
    void returnRequestEntity_WhenBuilt_ShouldSetAllFields() {
        ReturnRequestEntity returnReq = ReturnRequestEntity.builder()
                .orderItemId(1L)
                .reason("Defective product")
                .reasonDetails("The item arrived with scratches")
                .photos(List.of("photo1.jpg", "photo2.jpg"))
                .pickupAddress(Map.of("city", "Kathmandu"))
                .status(ReturnStatus.REQUESTED)
                .rmaNumber("RMA-ABC12345")
                .requestedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedBy(1L)
                .build();

        assertEquals("Defective product", returnReq.getReason());
        assertEquals(ReturnStatus.REQUESTED, returnReq.getStatus());
        assertEquals("RMA-ABC12345", returnReq.getRmaNumber());
        assertEquals(2, returnReq.getPhotos().size());
    }

    @Test
    void orderRefundEntity_WhenBuilt_ShouldSetAllFields() {
        OrderRefundEntity refund = OrderRefundEntity.builder()
                .orderId(1L)
                .refundAmount(new BigDecimal("500.00"))
                .status(RefundStatus.PENDING)
                .bankName("NMB Bank")
                .bankAccountName("John Doe")
                .accountNumber("1234567890")
                .branch("Thamel")
                .build();

        assertEquals(new BigDecimal("500.00"), refund.getRefundAmount());
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertEquals("NMB Bank", refund.getBankName());
        assertNull(refund.getRefundReference());
        assertNull(refund.getProcessedAt());
    }

    @Test
    void paymentTypeEntity_WhenSettersUsed_ShouldRetainValues() {
        PaymentTypeEntity paymentType = new PaymentTypeEntity();
        paymentType.setName("COD");
        paymentType.setDescription("Cash on delivery");
        paymentType.setIsActive(true);

        assertEquals("COD", paymentType.getName());
        assertEquals("Cash on delivery", paymentType.getDescription());
        assertTrue(paymentType.getIsActive());
    }

    @Test
    void cartEntity_WhenSettersUsed_ShouldRetainValues() {
        CartEntity cart = new CartEntity();
        cart.setUserId(42L);

        assertEquals(42L, cart.getUserId());
    }
}
