package com.nifilili.order.repository;

import com.nifilili.order.domain.CouponEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponEntity, Long> {

    Optional<CouponEntity> findByBusinessIdAndCode(Long businessId, String code);

    Page<CouponEntity> findByBusinessId(Long businessId, Pageable pageable);
}
