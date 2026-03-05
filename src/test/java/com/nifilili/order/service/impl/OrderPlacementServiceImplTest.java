package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.PaymentStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.CartEntity;
import com.nifilili.order.domain.CartItemEntity;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.domain.CouponUsageEntity;
import com.nifilili.order.domain.OrderEntity;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderPaymentEntity;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.request.AddressDto;
import com.nifilili.order.dto.request.CouponApplyRequest;
import com.nifilili.order.dto.request.PlaceOrderRequest;
import com.nifilili.order.dto.response.CartItemResponse;
import com.nifilili.order.dto.response.PlaceOrderResponse;
import com.nifilili.order.events.OrderPlacedEvent;
import com.nifilili.order.repository.CartItemRepository;
import com.nifilili.order.repository.CartRepository;
import com.nifilili.order.repository.CouponUsageRepository;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderPaymentRepository;
import com.nifilili.order.repository.OrderRepository;
import com.nifilili.order.repository.PaymentTypeRepository;
import com.nifilili.order.service.BusinessConfigService;
import com.nifilili.order.service.CartEnrichmentService;
import com.nifilili.order.service.CouponService;
import com.nifilili.order.service.PricingService;
import com.nifilili.order.service.PricingService.PricingResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPlacementServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long CART_ID = 10L;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderPaymentRepository orderPaymentRepository;

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private CartEnrichmentService cartEnrichmentService;

    @Mock
    private CouponService couponService;

    @Mock
    private BusinessConfigService businessConfigService;

    @Mock
    private PricingService pricingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderPlacementServiceImpl placementService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- placeOrder: COD ---

    @Test
    void placeOrder_WhenCodPayment_ShouldCreateOrderWithPendingPayment() {
        setupHappyPath("CASH_ON_DELIVERY");

        PlaceOrderRequest request = buildPlaceOrderRequest(1L, null);
        PlaceOrderResponse response = placementService.placeOrder(request);

        assertThat(response.getOrderNumber()).isNotNull();
        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("226.00"));

        // Verify payment entity has PENDING status
        ArgumentCaptor<OrderPaymentEntity> payCaptor = ArgumentCaptor.forClass(OrderPaymentEntity.class);
        verify(orderPaymentRepository).save(payCaptor.capture());
        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);

        // Verify cart was cleared
        verify(cartItemRepository).deleteByCartId(CART_ID);

        // Verify event published
        verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
    }

    // --- placeOrder: Bank Transfer ---

    @Test
    void placeOrder_WhenBankTransfer_ShouldCreateOrderWithPendingVerification() {
        setupHappyPath("Manual Bank Transfer");

        PlaceOrderRequest request = buildPlaceOrderRequest(1L, null);
        PlaceOrderResponse response = placementService.placeOrder(request);

        assertThat(response.getPaymentStatus()).isEqualTo("PENDING_VERIFICATION");

        ArgumentCaptor<OrderPaymentEntity> payCaptor = ArgumentCaptor.forClass(OrderPaymentEntity.class);
        verify(orderPaymentRepository).save(payCaptor.capture());
        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING_VERIFICATION);
    }

    // --- placeOrder: with coupon ---

    @Test
    void placeOrder_WhenCouponApplied_ShouldRecordCouponUsage() {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        cart.setUserId(USER_ID);
        CartItemEntity cartItem = TestEntityIdUtil.withId(
                CartItemEntity.builder().cartId(CART_ID).offeringId(100L).quantity(1).build(), 1L);
        CartItemResponse enriched = buildEnrichedItem(1L, 5L, new BigDecimal("100.00"), 1);
        PaymentTypeEntity paymentType = buildPaymentType(1L, "Cash on Delivery");
        CouponEntity coupon = TestEntityIdUtil.withId(
                CouponEntity.builder().code("SAVE10").build(), 50L);
        PricingResult pricing = buildPricingResult(new BigDecimal("100.00"), new BigDecimal("113.00"));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartEnrichmentService.enrich(any())).thenReturn(List.of(enriched));
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(paymentType));
        when(couponService.validateAndGet(5L, "SAVE10", USER_ID)).thenReturn(coupon);
        when(businessConfigService.getConfigMap(any())).thenReturn(Map.of(5L, Map.of()));
        when(pricingService.calculate(any(), any(), any())).thenReturn(pricing);
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(inv -> TestEntityIdUtil.withId(inv.getArgument(0), 500L));

        CouponApplyRequest couponReq = new CouponApplyRequest();
        couponReq.setBusinessId(5L);
        couponReq.setCouponCode("SAVE10");

        PlaceOrderRequest request = buildPlaceOrderRequest(1L, List.of(couponReq));
        placementService.placeOrder(request);

        // Verify coupon usage recorded
        verify(couponUsageRepository).save(any(CouponUsageEntity.class));
    }

    // --- placeOrder: cart not found ---

    @Test
    void placeOrder_WhenCartNotFound_ShouldThrowInvalidOrderState() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placementService.placeOrder(buildPlaceOrderRequest(1L, null)))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cart not found");
    }

    // --- placeOrder: empty cart ---

    @Test
    void placeOrder_WhenCartEmpty_ShouldThrowInvalidOrderState() {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        cart.setUserId(USER_ID);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> placementService.placeOrder(buildPlaceOrderRequest(1L, null)))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("empty");
    }

    // --- placeOrder: payment type not found ---

    @Test
    void placeOrder_WhenPaymentTypeNotFound_ShouldThrowResourceNotFound() {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        CartItemEntity cartItem = TestEntityIdUtil.withId(
                CartItemEntity.builder().cartId(CART_ID).offeringId(100L).quantity(1).build(), 1L);
        CartItemResponse enriched = buildEnrichedItem(1L, 5L, new BigDecimal("100.00"), 1);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartEnrichmentService.enrich(any())).thenReturn(List.of(enriched));
        when(paymentTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placementService.placeOrder(buildPlaceOrderRequest(999L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment type");
    }

    // --- helpers ---

    private void setupHappyPath(String paymentTypeName) {
        CartEntity cart = TestEntityIdUtil.withId(new CartEntity(), CART_ID);
        cart.setUserId(USER_ID);
        CartItemEntity cartItem = TestEntityIdUtil.withId(
                CartItemEntity.builder().cartId(CART_ID).offeringId(100L).quantity(2).build(), 1L);
        CartItemResponse enriched = buildEnrichedItem(1L, 5L, new BigDecimal("100.00"), 2);
        PaymentTypeEntity paymentType = buildPaymentType(1L, paymentTypeName);
        PricingResult pricing = buildPricingResult(new BigDecimal("200.00"), new BigDecimal("226.00"));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartEnrichmentService.enrich(any())).thenReturn(List.of(enriched));
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(paymentType));
        when(businessConfigService.getConfigMap(any())).thenReturn(Map.of(5L, Map.of()));
        when(pricingService.calculate(any(), any(), any())).thenReturn(pricing);
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(inv -> TestEntityIdUtil.withId(inv.getArgument(0), 500L));
    }

    private CartItemResponse buildEnrichedItem(Long cartItemId, Long businessId,
                                                BigDecimal unitPrice, int quantity) {
        return CartItemResponse.builder()
                .cartItemId(cartItemId).offeringId(cartItemId * 10).businessId(businessId)
                .title("Product #" + cartItemId).quantity(quantity).unitPrice(unitPrice)
                .available(true).subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .variantAttributes(Map.of()).build();
    }

    private PaymentTypeEntity buildPaymentType(Long id, String name) {
        PaymentTypeEntity type = new PaymentTypeEntity();
        type.setName(name);
        return TestEntityIdUtil.withId(type, id);
    }

    private PricingResult buildPricingResult(BigDecimal subtotal, BigDecimal payable) {
        BigDecimal tax = payable.subtract(subtotal);
        return new PricingResult(
                Map.of(1L, subtotal),
                Map.of(1L, tax),
                Map.of(1L, BigDecimal.ZERO),
                Map.of(1L, BigDecimal.ZERO),
                subtotal, tax, BigDecimal.ZERO, BigDecimal.ZERO, payable
        );
    }

    private PlaceOrderRequest buildPlaceOrderRequest(Long paymentTypeId,
                                                      List<CouponApplyRequest> coupons) {
        AddressDto address = new AddressDto();
        address.setMunicipalityId(1L);
        address.setWardNumber(5);
        address.setToleName("Kathmandu");
        address.setAddressField1("123 Main St");

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setReceiverName("John Doe");
        request.setContactNumber("9841234567");
        request.setEmail("john@example.com");
        request.setAddress(address);
        request.setPaymentTypeId(paymentTypeId);
        request.setCoupons(coupons);
        return request;
    }
}
