package com.nifilili.order.repository;

import com.nifilili.order.domain.PaymentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTypeRepository extends JpaRepository<PaymentTypeEntity, Long> {

    List<PaymentTypeEntity> findByIsActiveTrue();
}
