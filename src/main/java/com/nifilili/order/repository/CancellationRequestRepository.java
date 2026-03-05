package com.nifilili.order.repository;

import com.nifilili.core.enums.order.CancellationStatus;
import com.nifilili.order.domain.CancellationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancellationRequestRepository extends JpaRepository<CancellationRequestEntity, Long> {

    Optional<CancellationRequestEntity> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemIdAndStatus(Long orderItemId, CancellationStatus status);
}
