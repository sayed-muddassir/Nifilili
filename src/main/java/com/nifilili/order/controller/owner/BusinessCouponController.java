package com.nifilili.order.controller.owner;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CreateCouponRequest;
import com.nifilili.order.dto.response.CouponResponse;
import com.nifilili.order.service.CouponService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/business/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_10, description = "Business coupon management endpoints")
@Hidden
public class BusinessCouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Step 10.1: Create Coupon", description = "Creates a new coupon for the business")
    @ApiResponse(responseCode = "201", description = "Coupon created")
    @ApiResponse(responseCode = "400", description = "Invalid coupon data")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("POST /api/v1/business/coupons");
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(request));
    }

    @PutMapping("/{couponId}")
    @Operation(summary = "Step 10.2: Update Coupon", description = "Updates an existing coupon")
    @ApiResponse(responseCode = "200", description = "Coupon updated")
    @ApiResponse(responseCode = "404", description = "Coupon not found")
    public ResponseEntity<CouponResponse> updateCoupon(@PathVariable Long couponId,
                                                        @Valid @RequestBody CreateCouponRequest request) {
        log.info("PUT /api/v1/business/coupons/{}", couponId);
        return ResponseEntity.ok(couponService.updateCoupon(couponId, request));
    }

    @GetMapping
    @Operation(summary = "Step 10.3: List Coupons", description = "Returns paginated list of business coupons")
    @ApiResponse(responseCode = "200", description = "Coupons returned")
    public ResponseEntity<Page<CouponResponse>> listCoupons(Pageable pageable) {
        log.info("GET /api/v1/business/coupons");
        return ResponseEntity.ok(couponService.listCoupons(pageable));
    }

    @DeleteMapping("/{couponId}")
    @Operation(summary = "Step 10.4: Deactivate Coupon", description = "Deactivates a coupon (soft delete)")
    @ApiResponse(responseCode = "204", description = "Coupon deactivated")
    @ApiResponse(responseCode = "404", description = "Coupon not found")
    public ResponseEntity<Void> deactivate(@PathVariable Long couponId) {
        log.info("DELETE /api/v1/business/coupons/{}", couponId);
        couponService.deactivate(couponId);
        return ResponseEntity.noContent().build();
    }
}
