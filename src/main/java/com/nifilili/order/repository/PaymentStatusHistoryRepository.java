package com.nifilili.order.repository;

import com.nifilili.order.domain.PaymentStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistoryEntity, Long> {

    List<PaymentStatusHistoryEntity> findByPaymentIdOrderByCreatedAtAsc(Long paymentId);
}
