package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.dto.request.cart.AddCartItemRequest;
import com.nifilili.order.dto.request.cart.UpdateCartItemRequest;
import com.nifilili.order.dto.response.cart.CartItemResponse;
import com.nifilili.order.dto.response.cart.CartResponse;
import com.nifilili.order.dto.response.cart.CartSummaryResponse;
import com.nifilili.order.model.CartSnapshot;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartResponse getCurrentCart() {

        Long userId = SecurityUtil.getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(createCart(userId)));

        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());

        // In real system, enrich with product service
        List<CartItemResponse> responses = items.stream()
                .map(this::mapToResponse)
                .toList();

        CartSummaryResponse summary = new CartSummaryResponse();
        summary.setSubtotal(
                responses.stream()
                        .map(CartItemResponse::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setItems(responses);
        response.setSummary(summary);

        return response;
    }

    @Override
    public void addItem(AddCartItemRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(createCart(userId)));

        CartItemEntity item = new CartItemEntity();
        item.setCartId(cart.getId());
        item.setOfferingId(request.getOfferingId());
        item.setVariantId(request.getVariantId());
        item.setQuantity(request.getQuantity());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setCreatedBy(userId);
        item.setUpdatedBy(userId);

        cartItemRepository.save(item);
    }

    @Override
    public void updateQuantity(Long cartItemId, UpdateCartItemRequest request) {
        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        item.setQuantity(request.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(SecurityUtil.getCurrentUserId());
    }

    @Override
    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        cartRepository.findByUserId(userId)
                .ifPresent(cart ->
                        cartItemRepository.deleteAll(
                                cartItemRepository.findByCartId(cart.getId())
                        ));
    }

    /**
     * Snapshot cart for checkout.
     * This is where product availability & price validation happens.
     */
    @Override
    public CartSnapshot getValidatedCartSnapshot() {

        Long userId = SecurityUtil.getCurrentUserId();
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart is empty"));

        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            throw new IllegalStateException("Cart has no items");
        }

        // Normally, call Product/Offering service here
        return CartSnapshot.from(items);
    }

    private CartEntity createCart(Long userId) {
        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        return cart;
    }

    private CartItemResponse mapToResponse(CartItemEntity entity) {
        CartItemResponse response = new CartItemResponse();
        response.setCartItemId(entity.getId());
        response.setOfferingId(entity.getOfferingId());
        response.setVariantId(entity.getVariantId());
        response.setTitle("TODO get from offering service"); // mocked
        response.setQuantity(entity.getQuantity());
        response.setUnitPrice(BigDecimal.valueOf(500)); // mocked
        response.setSubtotal(response.getUnitPrice().multiply(BigDecimal.valueOf(response.getQuantity())));
        response.setAvailable(true);
        return response;
    }
}

