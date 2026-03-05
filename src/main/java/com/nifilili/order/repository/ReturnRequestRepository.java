package com.nifilili.order.repository;

import com.nifilili.order.domain.ReturnRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequestEntity, Long> {

    Optional<ReturnRequestEntity> findByOrderItemId(Long orderItemId);
}
