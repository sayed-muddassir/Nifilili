package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.dto.request.AddCartItemRequest;
import com.nifilili.order.dto.request.UpdateCartItemRequest;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.dto.response.CartResponse;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.service.CartEnrichmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long CART_ID = 10L;
    private static final Long CART_ITEM_ID = 100L;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartEnrichmentService cartEnrichmentService;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartEntity cart;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
        cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        cart.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- getCart ---

    @Test
    void getCart_WhenCartExists_ShouldReturnCartResponse() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        CartResponse response = cartService.getCart();

        assertThat(response.getCartId()).isEqualTo(CART_ID);
        assertThat(response.getItems()).isEmpty();
        verify(cartEnrichmentService, never()).enrich(any());
    }

    @Test
    void getCart_WhenCartNotExists_ShouldCreateNewCart() {
        CartEntity newCart = TestEntityIdUtil.withId(new CartEntity(), 20L);
        newCart.setUserId(USER_ID);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartEntity.class))).thenReturn(newCart);
        when(cartItemRepository.findByCartId(20L)).thenReturn(List.of());

        CartResponse response = cartService.getCart();

        assertThat(response.getCartId()).isEqualTo(20L);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void getCart_WhenCartHasItems_ShouldEnrichAndReturnSubtotal() {
        CartItemEntity item = buildCartItem(1L, 100L, null, 2);
        CartItemResponse enriched = buildEnrichedItem(1L, 100L, new BigDecimal("100.00"), 2, true);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(item));
        when(cartEnrichmentService.enrich(List.of(item))).thenReturn(List.of(enriched));

        CartResponse response = cartService.getCart();

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getSummary().getSubtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.getSummary().getWarnings()).isEmpty();
    }

    @Test
    void getCart_WhenItemUnavailable_ShouldIncludeWarning() {
        CartItemEntity item = buildCartItem(1L, 100L, null, 1);
        CartItemResponse enriched = buildEnrichedItem(1L, 100L, new BigDecimal("100.00"), 1, false);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(item));
        when(cartEnrichmentService.enrich(List.of(item))).thenReturn(List.of(enriched));

        CartResponse response = cartService.getCart();

        assertThat(response.getSummary().getWarnings()).hasSize(1);
        assertThat(response.getSummary().getWarnings().get(0)).contains("unavailable");
    }

    // --- addItem ---

    @Test
    void addItem_WhenNewItem_ShouldCreateCartItem() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setOfferingId(200L);
        request.setVariantId(null);
        request.setQuantity(3);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndOfferingIdAndVariantId(CART_ID, 200L, null))
                .thenReturn(Optional.empty());
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        cartService.addItem(request);

        ArgumentCaptor<CartItemEntity> captor = ArgumentCaptor.forClass(CartItemEntity.class);
        verify(cartItemRepository).save(captor.capture());
        CartItemEntity saved = captor.getValue();
        assertThat(saved.getCartId()).isEqualTo(CART_ID);
        assertThat(saved.getOfferingId()).isEqualTo(200L);
        assertThat(saved.getQuantity()).isEqualTo(3);
    }

    @Test
    void addItem_WhenExistingItem_ShouldIncrementQuantity() {
        CartItemEntity existing = buildCartItem(CART_ITEM_ID, 200L, null, 2);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setOfferingId(200L);
        request.setVariantId(null);
        request.setQuantity(3);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndOfferingIdAndVariantId(CART_ID, 200L, null))
                .thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        cartService.addItem(request);

        ArgumentCaptor<CartItemEntity> captor = ArgumentCaptor.forClass(CartItemEntity.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(5); // 2 + 3
    }

    // --- updateQuantity ---

    @Test
    void updateQuantity_WhenItemExists_ShouldUpdateQuantity() {
        CartItemEntity item = buildCartItem(CART_ITEM_ID, 100L, null, 2);
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        cartService.updateQuantity(CART_ITEM_ID, request);

        ArgumentCaptor<CartItemEntity> captor = ArgumentCaptor.forClass(CartItemEntity.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(5);
    }

    @Test
    void updateQuantity_WhenItemNotFound_ShouldThrowResourceNotFound() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(CART_ITEM_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- removeItem ---

    @Test
    void removeItem_WhenItemExists_ShouldDeleteItem() {
        CartItemEntity item = buildCartItem(CART_ITEM_ID, 100L, null, 2);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(item));

        cartService.removeItem(CART_ITEM_ID);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeItem_WhenItemNotFound_ShouldThrowResourceNotFound() {
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(CART_ITEM_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- clearCart ---

    @Test
    void clearCart_ShouldDeleteAllItemsForCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        cartService.clearCart();

        verify(cartItemRepository).deleteByCartId(CART_ID);
    }

    // --- helpers ---

    private CartItemEntity buildCartItem(Long id, Long offeringId, Long variantId, int quantity) {
        CartItemEntity item = CartItemEntity.builder()
                .cartId(CART_ID)
                .offeringId(offeringId)
                .variantId(variantId)
                .quantity(quantity)
                .build();
        return TestEntityIdUtil.withId(item, id);
    }

    private CartItemResponse buildEnrichedItem(Long cartItemId, Long offeringId,
                                                BigDecimal unitPrice, int quantity, boolean available) {
        return CartItemResponse.builder()
                .cartItemId(cartItemId)
                .offeringId(offeringId)
                .businessId(1L)
                .title("Offering #" + offeringId)
                .variantAttributes(Map.of())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .available(available)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}
