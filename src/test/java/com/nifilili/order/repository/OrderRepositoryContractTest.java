package com.nifilili.order.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRepositoryContractTest {

    @Test
    void cartRepository_ShouldExtendJpaRepositoryAndExposeFindByUserId() {
        assertTrue(JpaRepository.class.isAssignableFrom(CartRepository.class));
        assertMethodExists(CartRepository.class, "findByUserId", 1);
    }

    @Test
    void cartItemRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(CartItemRepository.class));
        assertMethodExists(CartItemRepository.class, "findByCartId", 1);
        assertMethodExists(CartItemRepository.class, "findByCartIdAndOfferingIdAndVariantId", 3);
        assertMethodExists(CartItemRepository.class, "deleteByCartId", 1);
    }

    @Test
    void orderRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(OrderRepository.class));
        assertMethodExists(OrderRepository.class, "findByUserId", 2);
        assertMethodExists(OrderRepository.class, "findByUserIdAndStatus", 3);
    }

    @Test
    void orderItemRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(OrderItemRepository.class));
        assertMethodExists(OrderItemRepository.class, "findByOrderId", 1);
        assertMethodExists(OrderItemRepository.class, "findByBusinessId", 2);
        assertMethodExists(OrderItemRepository.class, "findByBusinessIdAndStatus", 3);
        assertMethodExists(OrderItemRepository.class, "findByOrderIdAndStatusIn", 2);
    }

    @Test
    void orderStatusHistoryRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(OrderStatusHistoryRepository.class));
        assertMethodExists(OrderStatusHistoryRepository.class, "findByOrderItemIdOrderByCreatedAtAsc", 1);
    }

    @Test
    void orderPaymentRepository_ShouldExtendJpaRepositoryAndExposeFindByOrderId() {
        assertTrue(JpaRepository.class.isAssignableFrom(OrderPaymentRepository.class));
        assertMethodExists(OrderPaymentRepository.class, "findByOrderId", 1);
    }

    @Test
    void paymentStatusHistoryRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(PaymentStatusHistoryRepository.class));
        assertMethodExists(PaymentStatusHistoryRepository.class, "findByPaymentIdOrderByCreatedAtAsc", 1);
    }

    @Test
    void paymentTypeRepository_ShouldExtendJpaRepositoryAndExposeFindByIsActiveTrue() {
        assertTrue(JpaRepository.class.isAssignableFrom(PaymentTypeRepository.class));
        assertMethodExists(PaymentTypeRepository.class, "findByIsActiveTrue", 0);
    }

    @Test
    void cancellationRequestRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(CancellationRequestRepository.class));
        assertMethodExists(CancellationRequestRepository.class, "findByOrderItemId", 1);
        assertMethodExists(CancellationRequestRepository.class, "existsByOrderItemIdAndStatus", 2);
    }

    @Test
    void returnRequestRepository_ShouldExtendJpaRepositoryAndExposeFindByOrderItemId() {
        assertTrue(JpaRepository.class.isAssignableFrom(ReturnRequestRepository.class));
        assertMethodExists(ReturnRequestRepository.class, "findByOrderItemId", 1);
    }

    @Test
    void orderRefundRepository_ShouldExtendJpaRepositoryAndExposeFindByOrderId() {
        assertTrue(JpaRepository.class.isAssignableFrom(OrderRefundRepository.class));
        assertMethodExists(OrderRefundRepository.class, "findByOrderId", 1);
    }

    @Test
    void refundItemRepository_ShouldExtendJpaRepositoryAndExposeFindByRefundId() {
        assertTrue(JpaRepository.class.isAssignableFrom(RefundItemRepository.class));
        assertMethodExists(RefundItemRepository.class, "findByRefundId", 1);
    }

    @Test
    void couponRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(CouponRepository.class));
        assertMethodExists(CouponRepository.class, "findByBusinessIdAndCode", 2);
        assertMethodExists(CouponRepository.class, "findByBusinessId", 2);
    }

    @Test
    void couponUsageRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(CouponUsageRepository.class));
        assertMethodExists(CouponUsageRepository.class, "existsByCouponIdAndUserId", 2);
        assertMethodExists(CouponUsageRepository.class, "countByCouponId", 1);
    }

    @Test
    void businessConfigurationRepository_ShouldExtendJpaRepositoryAndExposeCustomMethods() {
        assertTrue(JpaRepository.class.isAssignableFrom(BusinessConfigurationRepository.class));
        assertMethodExists(BusinessConfigurationRepository.class, "findByBusinessId", 1);
        assertMethodExists(BusinessConfigurationRepository.class, "findByBusinessIdAndConfigKey", 2);
        assertMethodExists(BusinessConfigurationRepository.class, "findByBusinessIdIn", 1);
    }

    private void assertMethodExists(Class<?> clazz, String methodName, int paramCount) {
        Method method = Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == paramCount)
                .findFirst()
                .orElse(null);
        assertNotNull(method, clazz.getSimpleName() + "." + methodName + "(" + paramCount + " params) not found");
    }
}
