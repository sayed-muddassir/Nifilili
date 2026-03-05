package com.nifilili.order.repository;

import com.nifilili.order.domain.OrderPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPaymentEntity, Long> {

    Optional<OrderPaymentEntity> findByOrderId(Long orderId);
}
