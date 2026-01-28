package com.nifilili.order.repository;

import com.nifilili.order.domain.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    Page<OrderEntity> findByUserIdAndStatus(
            Long userId,
            String status,
            Pageable pageable
    );

    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}

