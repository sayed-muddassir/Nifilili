package com.nifilili.order.service;

import com.nifilili.order.domain.CouponEntity;
import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {

    /**
     * Creates a new coupon for the authenticated business owner.
     *
     * @param request the coupon details
     * @return the created coupon response
     */
    CouponResponse createCoupon(CreateCouponRequest request);

    /**
     * Updates an existing coupon owned by the authenticated business.
     *
     * @param couponId the coupon ID
     * @param request  the updated coupon details
     * @return the updated coupon response
     * @throws com.nifilili.core.exception.ResourceNotFoundException if coupon not found
     */
    CouponResponse updateCoupon(Long couponId, CreateCouponRequest request);

    /**
     * Lists all coupons for the authenticated business owner.
     *
     * @param pageable pagination parameters
     * @return page of coupon responses
     */
    Page<CouponResponse> listCoupons(Pageable pageable);

    /**
     * Deactivates a coupon owned by the authenticated business.
     *
     * @param couponId the coupon ID
     * @throws com.nifilili.core.exception.ResourceNotFoundException if coupon not found
     */
    void deactivate(Long couponId);

    /**
     * Validates a coupon for a specific business and user: checks existence, active status,
     * date validity, and one-per-user usage.
     *
     * @param businessId the business ID
     * @param code       the coupon code
     * @param userId     the user ID applying the coupon
     * @return the validated coupon entity
     * @throws com.nifilili.core.exception.InvalidOrderStateException if validation fails
     */
    CouponEntity validateAndGet(Long businessId, String code, Long userId);
}
