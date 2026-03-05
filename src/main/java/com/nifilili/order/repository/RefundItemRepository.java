package com.nifilili.order.repository;

import com.nifilili.order.domain.RefundItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundItemRepository extends JpaRepository<RefundItemEntity, Long> {

    List<RefundItemEntity> findByRefundId(Long refundId);
}
