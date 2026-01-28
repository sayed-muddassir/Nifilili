package com.nifilili.order.repository;

import com.nifilili.order.domain.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUserId(Long userId);
}

