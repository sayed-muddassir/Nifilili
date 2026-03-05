package com.nifilili.order.service.impl;

import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import com.nifilili.order.mapper.CouponMapper;
import com.nifilili.order.repository.CouponRepository;
import com.nifilili.order.repository.CouponUsageRepository;
import com.nifilili.order.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponMapper couponMapper;

    @Override
    public CouponResponse createCoupon(CreateCouponRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Creating coupon code={} by userId={}", request.getCode(), userId);

        CouponEntity entity = couponMapper.toEntity(request);
        entity.setBusinessId(userId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        return couponMapper.toResponse(couponRepository.save(entity));
    }

    @Override
    public CouponResponse updateCoupon(Long couponId, CreateCouponRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Updating coupon id={} by userId={}", couponId, userId);

        CouponEntity entity = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        entity.setCode(request.getCode());
        entity.setDiscountType(entity.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setMaxDiscount(request.getMaxDiscount());
        entity.setMinOrderAmount(request.getMinOrderAmount());
        entity.setValidFrom(request.getValidFrom());
        entity.setValidTo(request.getValidTo());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(userId);

        return couponMapper.toResponse(couponRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listCoupons(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Listing coupons for businessId={}", userId);
        return couponRepository.findByBusinessId(userId, pageable)
                .map(couponMapper::toResponse);
    }

    @Override
    public void deactivate(Long couponId) {
        log.info("Deactivating coupon id={}", couponId);
        CouponEntity entity = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        entity.setIsActive(false);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(SecurityUtil.getCurrentUserId());
        couponRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponEntity validateAndGet(Long businessId, String code, Long userId) {
        log.debug("Validating coupon code={} for businessId={}, userId={}", code, businessId, userId);

        CouponEntity coupon = couponRepository.findByBusinessIdAndCode(businessId, code)
                .orElseThrow(() -> new InvalidOrderStateException("Coupon not found: " + code));

        if (!coupon.getIsActive()) {
            throw new InvalidOrderStateException("Coupon is inactive: " + code);
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(coupon.getValidFrom()) || today.isAfter(coupon.getValidTo())) {
            throw new InvalidOrderStateException("Coupon is expired or not yet valid: " + code);
        }

        if (couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId)) {
            throw new InvalidOrderStateException("Coupon already used: " + code);
        }

        return coupon;
    }
}
