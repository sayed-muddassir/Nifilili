package com.nifilili.order.repository;

import com.nifilili.order.domain.OrderRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRefundRepository extends JpaRepository<OrderRefundEntity, Long> {

    Optional<OrderRefundEntity> findByOrderId(Long orderId);
}
