package com.nifilili.order.repository;

import com.nifilili.order.domain.RefundItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundItemRepository
        extends JpaRepository<RefundItemEntity, Long> {
}

