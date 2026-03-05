package com.nifilili.order.service.impl;

import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.request.CheckoutPreviewRequest;
import com.nifilili.order.dto.request.CouponApplyRequest;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.dto.response.CheckoutBusinessGroupResponse;
import com.nifilili.order.dto.response.CheckoutItemResponse;
import com.nifilili.order.dto.response.CheckoutPreviewResponse;
import com.nifilili.order.dto.response.CheckoutSummaryResponse;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.service.BusinessConfigService;
import com.nifilili.order.service.CartEnrichmentService;
import com.nifilili.order.service.CheckoutService;
import com.nifilili.order.service.CouponService;
import com.nifilili.order.service.PricingService;
import com.nifilili.order.service.PricingService.PricingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartEnrichmentService cartEnrichmentService;
    private final CouponService couponService;
    private final BusinessConfigService businessConfigService;
    private final PricingService pricingService;

    @Override
    public CheckoutPreviewResponse preview(CheckoutPreviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Generating checkout preview for userId={}", userId);

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidOrderStateException("Cart not found"));

        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new InvalidOrderStateException("Cart is empty");
        }

        // Enrich cart items with offering data
        List<CartItemResponse> enrichedItems = cartEnrichmentService.enrich(cartItems);

        // Collect business IDs
        Set<Long> businessIds = enrichedItems.stream()
                .map(CartItemResponse::getBusinessId)
                .collect(Collectors.toSet());

        // Resolve coupons
        Map<Long, CouponEntity> coupons = resolveCoupons(request.getCoupons(), userId);

        // Get business configs
        Map<Long, Map<String, String>> businessConfigs = businessConfigService.getConfigMap(businessIds);

        // Calculate pricing
        PricingResult pricing = pricingService.calculate(enrichedItems, coupons, businessConfigs);

        // Group items by business
        Map<Long, List<CartItemResponse>> itemsByBusiness = enrichedItems.stream()
                .collect(Collectors.groupingBy(CartItemResponse::getBusinessId));

        List<CheckoutBusinessGroupResponse> businessGroups = new ArrayList<>();
        for (Map.Entry<Long, List<CartItemResponse>> entry : itemsByBusiness.entrySet()) {
            Long businessId = entry.getKey();
            List<CartItemResponse> businessItems = entry.getValue();

            BigDecimal groupDelivery = BigDecimal.ZERO;
            BigDecimal groupTax = BigDecimal.ZERO;
            BigDecimal groupDiscount = BigDecimal.ZERO;

            List<CheckoutItemResponse> checkoutItems = new ArrayList<>();
            for (CartItemResponse item : businessItems) {
                Long itemId = item.getCartItemId();
                groupDelivery = groupDelivery.add(pricing.itemDelivery().getOrDefault(itemId, BigDecimal.ZERO));
                groupTax = groupTax.add(pricing.itemTaxes().getOrDefault(itemId, BigDecimal.ZERO));
                groupDiscount = groupDiscount.add(pricing.itemDiscounts().getOrDefault(itemId, BigDecimal.ZERO));

                checkoutItems.add(CheckoutItemResponse.builder()
                        .offeringId(item.getOfferingId())
                        .title(item.getTitle())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(pricing.itemSubtotals().getOrDefault(itemId, BigDecimal.ZERO))
                        .build());
            }

            businessGroups.add(CheckoutBusinessGroupResponse.builder()
                    .businessId(businessId)
                    .businessName("Business #" + businessId) // TODO: resolve from business module
                    .items(checkoutItems)
                    .deliveryCharge(groupDelivery)
                    .tax(groupTax)
                    .discount(groupDiscount)
                    .build());
        }

        CheckoutSummaryResponse summary = CheckoutSummaryResponse.builder()
                .subtotal(pricing.subtotal())
                .totalDiscount(pricing.totalDiscount())
                .totalTax(pricing.totalTax())
                .deliveryCharge(pricing.totalDelivery())
                .payableAmount(pricing.payableAmount())
                .build();

        return CheckoutPreviewResponse.builder()
                .businessGroups(businessGroups)
                .summary(summary)
                .build();
    }

    private Map<Long, CouponEntity> resolveCoupons(List<CouponApplyRequest> couponRequests, Long userId) {
        Map<Long, CouponEntity> coupons = new HashMap<>();
        if (couponRequests == null || couponRequests.isEmpty()) {
            return coupons;
        }
        for (CouponApplyRequest req : couponRequests) {
            CouponEntity coupon = couponService.validateAndGet(req.getBusinessId(), req.getCouponCode(), userId);
            coupons.put(req.getBusinessId(), coupon);
        }
        return coupons;
    }
}
