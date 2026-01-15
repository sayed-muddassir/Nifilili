package com.nifilili.offering.repository;

import com.nifilili.core.enums.offering.OfferingStatus;
import com.nifilili.offering.domain.OfferingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferingRepository
        extends JpaRepository<OfferingEntity, Long> {

    // -------------------------
    // Ownership & listing
    // -------------------------
    List<OfferingEntity> findByOwnerTypeAndOwnerId(
            String ownerType,
            Long ownerId
    );

    Page<OfferingEntity> findByOwnerTypeAndOwnerId(
            String ownerType,
            Long ownerId,
            Pageable pageable
    );

    // -------------------------
    // Category based browsing
    // -------------------------
    Page<OfferingEntity> findByCategoryIdAndStatus(
            Long categoryId,
            String status,
            Pageable pageable
    );

    // -------------------------
    // Public visibility
    // -------------------------
    Optional<OfferingEntity> findByIdAndStatus(
            Long id,
            OfferingStatus status
    );

    List<OfferingEntity> findByIsFeaturedTrueAndStatus(
            String status
    );

    long countByOwnerTypeAndOwnerIdAndIsFeaturedTrue(
            String ownerType,
            Long ownerId
    );

    // -------------------------
    // SKU checks
    // -------------------------
    boolean existsBySku(String sku);

    // -------------------------
    // Inventory
    // -------------------------
    @Modifying
    @Query("""
        update OfferingEntity o
        set o.availableQuantity = :quantity
        where o.id = :offeringId
    """)
    void updateInventory(
            @Param("offeringId") Long offeringId,
            @Param("quantity") Integer quantity
    );

    // -------------------------
    // View count
    // -------------------------
    @Modifying
    @Query("""
        update OfferingEntity o
        set o.viewCount = o.viewCount + 1
        where o.id = :offeringId
    """)
    void incrementViewCount(@Param("offeringId") Long offeringId);
}
