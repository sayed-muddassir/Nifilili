package com.nifilili.order.repository;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.order.domain.OrderItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByOrderId(Long orderId);

    Page<OrderItemEntity> findByBusinessId(Long businessId, Pageable pageable);

    Page<OrderItemEntity> findByBusinessIdAndStatus(Long businessId, OrderItemStatus status, Pageable pageable);

    List<OrderItemEntity> findByOrderIdAndStatusIn(Long orderId, List<OrderItemStatus> statuses);
}
