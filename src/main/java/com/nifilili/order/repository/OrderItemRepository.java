package com.nifilili.order.repository;

import com.nifilili.order.domain.OrderItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByOrderId(Long orderId);

    Page<OrderItemEntity> findByBusinessIdAndStatus(Long businessId, String status, Pageable pageable);

    Page<OrderItemEntity> findByBusinessId(Long businessId, Pageable pageable);

    @Query("""
        SELECT oi FROM OrderItemEntity oi
        WHERE oi.orderId = :orderId
        AND oi.status IN ('CANCELLED','RETURN_APPROVED')
    """)
    List<OrderItemEntity> findApprovedForRefund(Long orderId);
}

