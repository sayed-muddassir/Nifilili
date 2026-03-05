package com.nifilili.order.service.impl;

import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.dto.request.AddCartItemRequest;
import com.nifilili.order.dto.request.UpdateCartItemRequest;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.dto.response.CartResponse;
import com.nifilili.order.dto.response.CartSummaryResponse;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.service.CartEnrichmentService;
import com.nifilili.order.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartEnrichmentService cartEnrichmentService;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Getting cart for userId={}", userId);

        CartEntity cart = getOrCreateCart(userId);
        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        return buildCartResponse(cart, cartItems);
    }

    @Override
    public CartResponse addItem(AddCartItemRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Adding item to cart: offeringId={}, userId={}", request.getOfferingId(), userId);

        CartEntity cart = getOrCreateCart(userId);

        // Check if same offering+variant already in cart
        Optional<CartItemEntity> existing = cartItemRepository
                .findByCartIdAndOfferingIdAndVariantId(
                        cart.getId(), request.getOfferingId(), request.getVariantId());

        if (existing.isPresent()) {
            CartItemEntity item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setUpdatedAt(LocalDateTime.now());
            item.setUpdatedBy(userId);
            cartItemRepository.save(item);
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .cartId(cart.getId())
                    .offeringId(request.getOfferingId())
                    .variantId(request.getVariantId())
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .createdBy(userId)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(userId)
                    .build();
            cartItemRepository.save(newItem);
        }

        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        return buildCartResponse(cart, cartItems);
    }

    @Override
    public CartResponse updateQuantity(Long cartItemId, UpdateCartItemRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Updating cart item quantity: cartItemId={}, quantity={}", cartItemId, request.getQuantity());

        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setQuantity(request.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        cartItemRepository.save(item);

        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        return buildCartResponse(cart, cartItems);
    }

    @Override
    public void removeItem(Long cartItemId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Removing cart item: cartItemId={}, userId={}", cartItemId, userId);

        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Clearing cart for userId={}", userId);

        CartEntity cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartEntity getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse buildCartResponse(CartEntity cart, List<CartItemEntity> cartItems) {
        List<CartItemResponse> enrichedItems = cartItems.isEmpty()
                ? new ArrayList<>()
                : cartEnrichmentService.enrich(cartItems);

        BigDecimal subtotal = enrichedItems.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> warnings = enrichedItems.stream()
                .filter(item -> !item.getAvailable())
                .map(item -> "Item '" + item.getTitle() + "' is currently unavailable")
                .toList();

        CartSummaryResponse summary = CartSummaryResponse.builder()
                .subtotal(subtotal)
                .warnings(warnings)
                .build();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(enrichedItems)
                .summary(summary)
                .build();
    }
}
