package com.nifilili.offering.repository;

import com.nifilili.offering.domain.OfferingVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingVariantRepository
        extends JpaRepository<OfferingVariantEntity, Long> {

    // All variants for an offering
    List<OfferingVariantEntity> findByOfferingId(Long offeringId);

    // Paginated variants for an offering
    Page<OfferingVariantEntity> findByOfferingId(Long offeringId, Pageable pageable);

    // Active variants only
    List<OfferingVariantEntity> findByOfferingIdAndStatus(
            Long offeringId,
            String status
    );

    // SKU uniqueness (per offering)
    boolean existsByOfferingIdAndSku(Long offeringId, String sku);

    // Check if offering has variants
    boolean existsByOfferingId(Long offeringId);

    // Inventory update
    @Modifying
    @Query("""
        update OfferingVariantEntity v
        set v.availableQuantity = :quantity
        where v.id = :variantId
    """)
    void updateInventory(
            @Param("variantId") Long variantId,
            @Param("quantity") Long quantity
    );
}

