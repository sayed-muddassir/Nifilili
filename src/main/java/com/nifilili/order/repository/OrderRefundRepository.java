package com.nifilili.order.repository;

import com.nifilili.order.domain.OrderRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRefundRepository
        extends JpaRepository<OrderRefundEntity, Long> {
}

