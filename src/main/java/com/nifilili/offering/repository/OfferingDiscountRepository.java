package com.nifilili.offering.repository;

import com.nifilili.offering.domain.OfferingDiscountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferingDiscountRepository
        extends JpaRepository<OfferingDiscountEntity, Long> {

    // Active discount for offering
    Optional<OfferingDiscountEntity> findFirstByOfferingIdAndVariantIsNullAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long offeringId,
            String status,
            LocalDateTime now1,
            LocalDateTime now2
    );

    // Active discount for variant
    Optional<OfferingDiscountEntity> findFirstByVariantIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long variantId,
            String status,
            LocalDateTime now1,
            LocalDateTime now2
    );

    // List discounts for management
    List<OfferingDiscountEntity> findByOfferingId(Long offeringId);

    // Paginated discounts for management
    Page<OfferingDiscountEntity> findByOfferingId(Long offeringId, Pageable pageable);
}
