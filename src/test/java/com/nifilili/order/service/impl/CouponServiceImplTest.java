package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.util.DiscountType;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import com.nifilili.order.mapper.CouponMapper;
import com.nifilili.order.repository.CouponRepository;
import com.nifilili.order.repository.CouponUsageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long COUPON_ID = 100L;
    private static final Long BUSINESS_ID = 5L;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- createCoupon ---

    @Test
    void createCoupon_WhenValidRequest_ShouldCreateAndReturnCoupon() {
        CreateCouponRequest request = buildCreateRequest("SAVE10");
        CouponEntity mapped = new CouponEntity();
        CouponEntity saved = TestEntityIdUtil.withId(new CouponEntity(), COUPON_ID);
        CouponResponse expectedResponse = CouponResponse.builder().id(COUPON_ID).code("SAVE10").build();

        when(couponMapper.toEntity(request)).thenReturn(mapped);
        when(couponRepository.save(any(CouponEntity.class))).thenReturn(saved);
        when(couponMapper.toResponse(saved)).thenReturn(expectedResponse);

        CouponResponse response = couponService.createCoupon(request);

        assertThat(response.getCode()).isEqualTo("SAVE10");
        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getBusinessId()).isEqualTo(USER_ID);
    }

    // --- updateCoupon ---

    @Test
    void updateCoupon_WhenCouponExists_ShouldUpdate() {
        CouponEntity existing = buildCoupon(COUPON_ID, "OLD_CODE", true);
        CreateCouponRequest request = buildCreateRequest("NEW_CODE");
        CouponResponse expectedResponse = CouponResponse.builder().id(COUPON_ID).code("NEW_CODE").build();

        when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(existing));
        when(couponRepository.save(any(CouponEntity.class))).thenReturn(existing);
        when(couponMapper.toResponse(existing)).thenReturn(expectedResponse);

        CouponResponse response = couponService.updateCoupon(COUPON_ID, request);

        assertThat(response.getCode()).isEqualTo("NEW_CODE");
    }

    @Test
    void updateCoupon_WhenCouponNotFound_ShouldThrowResourceNotFound() {
        when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.updateCoupon(COUPON_ID, buildCreateRequest("X")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- listCoupons ---

    @Test
    void listCoupons_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        CouponEntity coupon = buildCoupon(COUPON_ID, "SAVE10", true);
        CouponResponse response = CouponResponse.builder().id(COUPON_ID).code("SAVE10").build();

        when(couponRepository.findByBusinessId(USER_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(coupon)));
        when(couponMapper.toResponse(coupon)).thenReturn(response);

        Page<CouponResponse> result = couponService.listCoupons(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("SAVE10");
    }

    // --- deactivate ---

    @Test
    void deactivate_WhenCouponExists_ShouldSetInactive() {
        CouponEntity coupon = buildCoupon(COUPON_ID, "SAVE10", true);
        when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(coupon));

        couponService.deactivate(COUPON_ID);

        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isFalse();
    }

    @Test
    void deactivate_WhenCouponNotFound_ShouldThrowResourceNotFound() {
        when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.deactivate(COUPON_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- validateAndGet ---

    @Test
    void validateAndGet_WhenValidCoupon_ShouldReturnCoupon() {
        CouponEntity coupon = buildCoupon(COUPON_ID, "VALID", true);
        coupon.setValidFrom(LocalDate.now().minusDays(1));
        coupon.setValidTo(LocalDate.now().plusDays(1));

        when(couponRepository.findByBusinessIdAndCode(BUSINESS_ID, "VALID"))
                .thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCouponIdAndUserId(COUPON_ID, USER_ID)).thenReturn(false);

        CouponEntity result = couponService.validateAndGet(BUSINESS_ID, "VALID", USER_ID);

        assertThat(result.getCode()).isEqualTo("VALID");
    }

    @Test
    void validateAndGet_WhenCouponNotFound_ShouldThrowInvalidOrderState() {
        when(couponRepository.findByBusinessIdAndCode(BUSINESS_ID, "MISSING"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.validateAndGet(BUSINESS_ID, "MISSING", USER_ID))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("MISSING");
    }

    @Test
    void validateAndGet_WhenCouponInactive_ShouldThrowInvalidOrderState() {
        CouponEntity coupon = buildCoupon(COUPON_ID, "INACTIVE", false);

        when(couponRepository.findByBusinessIdAndCode(BUSINESS_ID, "INACTIVE"))
                .thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateAndGet(BUSINESS_ID, "INACTIVE", USER_ID))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void validateAndGet_WhenCouponExpired_ShouldThrowInvalidOrderState() {
        CouponEntity coupon = buildCoupon(COUPON_ID, "EXPIRED", true);
        coupon.setValidFrom(LocalDate.now().minusDays(30));
        coupon.setValidTo(LocalDate.now().minusDays(1));

        when(couponRepository.findByBusinessIdAndCode(BUSINESS_ID, "EXPIRED"))
                .thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateAndGet(BUSINESS_ID, "EXPIRED", USER_ID))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateAndGet_WhenCouponAlreadyUsed_ShouldThrowInvalidOrderState() {
        CouponEntity coupon = buildCoupon(COUPON_ID, "USED", true);
        coupon.setValidFrom(LocalDate.now().minusDays(1));
        coupon.setValidTo(LocalDate.now().plusDays(1));

        when(couponRepository.findByBusinessIdAndCode(BUSINESS_ID, "USED"))
                .thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCouponIdAndUserId(COUPON_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> couponService.validateAndGet(BUSINESS_ID, "USED", USER_ID))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already used");
    }

    // --- helpers ---

    private CouponEntity buildCoupon(Long id, String code, boolean active) {
        CouponEntity coupon = CouponEntity.builder()
                .businessId(BUSINESS_ID)
                .code(code)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .isActive(active)
                .validFrom(LocalDate.now().minusDays(5))
                .validTo(LocalDate.now().plusDays(5))
                .build();
        return TestEntityIdUtil.withId(coupon, id);
    }

    private CreateCouponRequest buildCreateRequest(String code) {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode(code);
        request.setDiscountType(String.valueOf(DiscountType.PERCENTAGE));
        request.setDiscountValue(new BigDecimal("10"));
        request.setValidFrom(LocalDate.now().minusDays(1));
        request.setValidTo(LocalDate.now().plusDays(30));
        return request;
    }
}
