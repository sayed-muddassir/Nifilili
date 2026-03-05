package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.request.AddressDto;
import com.nifilili.order.dto.request.CheckoutPreviewRequest;
import com.nifilili.order.dto.request.CouponApplyRequest;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.dto.response.CheckoutPreviewResponse;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.service.BusinessConfigService;
import com.nifilili.order.service.CartEnrichmentService;
import com.nifilili.order.service.CouponService;
import com.nifilili.order.service.PricingService;
import com.nifilili.order.service.PricingService.PricingResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long CART_ID = 10L;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartEnrichmentService cartEnrichmentService;

    @Mock
    private CouponService couponService;

    @Mock
    private BusinessConfigService businessConfigService;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- preview ---

    @Test
    void preview_WhenValidCart_ShouldReturnPreviewResponse() {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        cart.setUserId(USER_ID);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cartId(CART_ID).offeringId(100L).quantity(2).build();
        TestEntityIdUtil.withId(cartItem, 1L);

        CartItemResponse enriched = CartItemResponse.builder()
                .cartItemId(1L).offeringId(100L).businessId(5L)
                .title("Test Product").quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .available(true)
                .subtotal(new BigDecimal("200.00"))
                .build();

        PricingResult pricing = new PricingResult(
                Map.of(1L, new BigDecimal("200.00")),   // itemSubtotals
                Map.of(1L, new BigDecimal("26.00")),    // itemTaxes
                Map.of(1L, BigDecimal.ZERO),            // itemDelivery
                Map.of(1L, BigDecimal.ZERO),            // itemDiscounts
                new BigDecimal("200.00"),                // subtotal
                new BigDecimal("26.00"),                 // totalTax
                BigDecimal.ZERO,                         // totalDelivery
                BigDecimal.ZERO,                         // totalDiscount
                new BigDecimal("226.00")                 // payableAmount
        );

        CheckoutPreviewRequest request = buildPreviewRequest(null);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartEnrichmentService.enrich(List.of(cartItem))).thenReturn(List.of(enriched));
        when(businessConfigService.getConfigMap(Set.of(5L))).thenReturn(Map.of(5L, Map.of()));
        when(pricingService.calculate(eq(List.of(enriched)), any(), any())).thenReturn(pricing);

        CheckoutPreviewResponse response = checkoutService.preview(request);

        assertThat(response.getBusinessGroups()).hasSize(1);
        assertThat(response.getBusinessGroups().get(0).getBusinessId()).isEqualTo(5L);
        assertThat(response.getSummary().getSubtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.getSummary().getPayableAmount()).isEqualByComparingTo(new BigDecimal("226.00"));
    }

    @Test
    void preview_WhenCartNotFound_ShouldThrowInvalidOrderState() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.preview(buildPreviewRequest(null)))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cart not found");
    }

    @Test
    void preview_WhenCartEmpty_ShouldThrowInvalidOrderState() {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        cart.setUserId(USER_ID);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> checkoutService.preview(buildPreviewRequest(null)))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void preview_WhenCouponsProvided_ShouldResolveCoupons() {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        CartItemEntity cartItem = TestEntityIdUtil.withId(
                CartItemEntity.builder().cartId(CART_ID).offeringId(100L).quantity(1).build(), 1L);
        CartItemResponse enriched = CartItemResponse.builder()
                .cartItemId(1L).offeringId(100L).businessId(5L)
                .title("Product").quantity(1).unitPrice(new BigDecimal("100.00"))
                .available(true).subtotal(new BigDecimal("100.00")).build();

        CouponEntity coupon = CouponEntity.builder().code("SAVE10").build();
        TestEntityIdUtil.withId(coupon, 50L);

        PricingResult pricing = new PricingResult(
                Map.of(1L, new BigDecimal("100.00")),
                Map.of(1L, new BigDecimal("11.70")),
                Map.of(1L, BigDecimal.ZERO),
                Map.of(1L, new BigDecimal("10.00")),
                new BigDecimal("100.00"),
                new BigDecimal("11.70"),
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                new BigDecimal("101.70")
        );

        CouponApplyRequest couponReq = new CouponApplyRequest();
        couponReq.setBusinessId(5L);
        couponReq.setCouponCode("SAVE10");

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartEnrichmentService.enrich(List.of(cartItem))).thenReturn(List.of(enriched));
        when(couponService.validateAndGet(5L, "SAVE10", USER_ID)).thenReturn(coupon);
        when(businessConfigService.getConfigMap(Set.of(5L))).thenReturn(Map.of(5L, Map.of()));
        when(pricingService.calculate(any(), any(), any())).thenReturn(pricing);

        CheckoutPreviewResponse response = checkoutService.preview(
                buildPreviewRequest(List.of(couponReq)));

        assertThat(response.getSummary().getTotalDiscount())
                .isEqualByComparingTo(new BigDecimal("10.00"));
    }

    // --- helpers ---

    private CheckoutPreviewRequest buildPreviewRequest(List<CouponApplyRequest> coupons) {
        AddressDto address = new AddressDto();
        address.setMunicipalityId(1L);
        address.setWardNumber(5);
        address.setToleName("Kathmandu");
        address.setAddressField1("123 Main St");

        CheckoutPreviewRequest request = new CheckoutPreviewRequest();
        request.setAddress(address);
        request.setCoupons(coupons);
        return request;
    }
}
