package com.nifilili.order.repository;

import com.nifilili.order.domain.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByCartId(Long cartId);

    Optional<CartItemEntity> findByCartIdAndOfferingIdAndVariantId(Long cartId, Long offeringId, Long variantId);

    void deleteByCartId(Long cartId);
}
