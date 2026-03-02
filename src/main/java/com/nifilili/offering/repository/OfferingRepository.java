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

import com.nifilili.core.enums.offering.OfferingType;

import java.math.BigDecimal;
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

    // Paginated featured offerings (enum status)
    Page<OfferingEntity> findByIsFeaturedTrueAndStatus(
            OfferingStatus status,
            Pageable pageable
    );

    // Admin: list all offerings by status
    Page<OfferingEntity> findByStatus(
            OfferingStatus status,
            Pageable pageable
    );

    // Check if offerings use a specific category (for safe category delete)
    boolean existsByCategoryId(Long categoryId);

    long countByOwnerTypeAndOwnerIdAndIsFeaturedTrue(
            String ownerType,
            Long ownerId
    );

    // -------------------------
    // Owner listing (by ownerId)
    // -------------------------
    Page<OfferingEntity> findByOwnerId(
            Long ownerId,
            Pageable pageable
    );

    Page<OfferingEntity> findByOwnerIdAndStatus(
            Long ownerId,
            OfferingStatus status,
            Pageable pageable
    );

    // -------------------------
    // Public search (all filters optional)
    // -------------------------
    @Query("""
        SELECT o FROM OfferingEntity o
        WHERE o.status = 'PUBLISHED'
          AND (:categoryId IS NULL OR o.category.id = :categoryId)
          AND (:type IS NULL OR o.type = :type)
          AND (:minPrice IS NULL OR o.price >= :minPrice)
          AND (:maxPrice IS NULL OR o.price <= :maxPrice)
    """)
    Page<OfferingEntity> searchPublished(
            @Param("categoryId") Long categoryId,
            @Param("type") OfferingType type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // -------------------------
    // Category-based browsing (enum status)
    // -------------------------
    Page<OfferingEntity> findByCategoryIdAndStatus(
            Long categoryId,
            OfferingStatus status,
            Pageable pageable
    );

    // -------------------------
    // SKU checks (unique per owner)
    // -------------------------
    boolean existsByOwnerIdAndSku(Long ownerId, String sku);

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
