package com.nifilili.order.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.dto.response.CartItemResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CartEnrichmentServiceImplTest {

    @InjectMocks
    private CartEnrichmentServiceImpl enrichmentService;

    @Test
    void enrich_WhenSingleItem_ShouldReturnEnrichedResponse() {
        CartItemEntity item = CartItemEntity.builder()
                .cartId(1L)
                .offeringId(100L)
                .variantId(null)
                .quantity(3)
                .build();
        TestEntityIdUtil.withId(item, 10L);

        List<CartItemResponse> result = enrichmentService.enrich(List.of(item));

        assertThat(result).hasSize(1);
        CartItemResponse enriched = result.get(0);
        assertThat(enriched.getCartItemId()).isEqualTo(10L);
        assertThat(enriched.getOfferingId()).isEqualTo(100L);
        assertThat(enriched.getBusinessId()).isEqualTo(1L); // Mock value
        assertThat(enriched.getTitle()).isEqualTo("Offering #100");
        assertThat(enriched.getUnitPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(enriched.getQuantity()).isEqualTo(3);
        assertThat(enriched.getSubtotal()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(enriched.getAvailable()).isTrue();
        assertThat(enriched.getVariantAttributes()).isEmpty();
    }

    @Test
    void enrich_WhenItemWithVariant_ShouldIncludeVariantAttributes() {
        CartItemEntity item = CartItemEntity.builder()
                .cartId(1L)
                .offeringId(200L)
                .variantId(50L)
                .quantity(1)
                .build();
        TestEntityIdUtil.withId(item, 20L);

        List<CartItemResponse> result = enrichmentService.enrich(List.of(item));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVariantId()).isEqualTo(50L);
        assertThat(result.get(0).getVariantAttributes()).containsEntry("variantId", 50L);
    }

    @Test
    void enrich_WhenMultipleItems_ShouldReturnAll() {
        CartItemEntity item1 = TestEntityIdUtil.withId(
                CartItemEntity.builder().cartId(1L).offeringId(100L).quantity(1).build(), 10L);
        CartItemEntity item2 = TestEntityIdUtil.withId(
                CartItemEntity.builder().cartId(1L).offeringId(200L).quantity(2).build(), 20L);

        List<CartItemResponse> result = enrichmentService.enrich(List.of(item1, item2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOfferingId()).isEqualTo(100L);
        assertThat(result.get(1).getOfferingId()).isEqualTo(200L);
    }

    @Test
    void enrich_WhenEmptyList_ShouldReturnEmptyList() {
        List<CartItemResponse> result = enrichmentService.enrich(List.of());

        assertThat(result).isEmpty();
    }
}
