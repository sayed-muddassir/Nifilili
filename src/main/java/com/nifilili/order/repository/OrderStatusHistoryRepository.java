package com.nifilili.order.repository;

import com.nifilili.order.domain.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistoryEntity, Long> {

    List<OrderStatusHistoryEntity> findByOrderItemIdOrderByCreatedAtAsc(Long orderItemId);
}

