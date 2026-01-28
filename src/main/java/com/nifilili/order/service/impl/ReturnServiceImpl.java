package com.nifilili.order.service.impl;

import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.dto.request.returnflow.CreateReturnRequest;
import com.nifilili.order.dto.response.returnflow.ReturnResponse;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.ReturnRequestRepository;
import com.nifilili.order.service.returnflow.ReturnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;

    /**
     * Item states eligible for return.
     */
    private static final Set<String> RETURN_ELIGIBLE_STATES = Set.of(
            "delivered"
    );

    // ------------------------------------------------------------------
    // CREATE RETURN REQUEST (USER ACTION)
    // ------------------------------------------------------------------

    /**
     * Create a return request for a delivered order item.
     *
     * IMPORTANT BUSINESS RULES:
     * - Only delivered items can be returned
     * - Return window must be valid
     * - One return per order item
     * - No refund is created here
     */
    @Override
    @Transactional
    public ReturnResponse createReturn(
            Long orderItemId,
            CreateReturnRequest request
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        // 1️⃣ Fetch order item
        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order item not found"));

        // 🔐 Ownership check (customer)
        if (!item.getCreatedBy().equals(userId)) {
            throw new IllegalStateException(
                    "Unauthorized return request"
            );
        }

        // 🚫 Return eligibility check
        if (!RETURN_ELIGIBLE_STATES.contains(item.getStatus())) {
            throw new IllegalStateException(
                    "Item not eligible for return in state: " + item.getStatus()
            );
        }

        // 🚫 Prevent duplicate return request
        returnRequestRepository.findByOrderItemId(orderItemId)
                .ifPresent(r ->
                        throwDuplicateReturnException()
                );

        // 🚫 Return window check (mocked)
        if (!isWithinReturnWindow(item)) {
            throw new IllegalStateException(
                    "Return window has expired"
            );
        }

        // 2️⃣ Create return request
        ReturnRequestEntity returnRequest = new ReturnRequestEntity();
        returnRequest.setOrderItemId(orderItemId);
        returnRequest.setReason(request.getReason());
        returnRequest.setReasonDetails(request.getDetails());
        returnRequest.setPhotos(request.getPhotos());
        returnRequest.setPickupAddress(Map.of("municipalityId",
                request.getPickupAddress().getMunicipalityId(),
                "wardNumber",
                request.getPickupAddress().getWardNumber(),
                "toleName",
                request.getPickupAddress().getToleName(),
                "addressField1",
                request.getPickupAddress().getAddressField1(),
                "postalCode",
                request.getPickupAddress().getPostalCode()
        ));

        returnRequest.setStatus("requested");
        returnRequest.setRmaNumber(generateRma());

        returnRequest.setRequestedAt(LocalDateTime.now());
        returnRequest.setCreatedAt(LocalDateTime.now());
        returnRequest.setUpdatedAt(LocalDateTime.now());
        returnRequest.setCreatedBy(userId);
        returnRequest.setUpdatedBy(userId);

        returnRequestRepository.save(returnRequest);

        return new ReturnResponse(
                returnRequest.getId(),
                returnRequest.getRmaNumber(),
                returnRequest.getStatus()
        );
    }

    // ------------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------------

    private void throwDuplicateReturnException() {
        throw new IllegalStateException(
                "Return request already exists for this item"
        );
    }

    /**
     * Mock return window validation.
     *
     * Replace with:
     * - business config lookup
     * - delivered_at timestamp
     */
    private boolean isWithinReturnWindow(OrderItemEntity item) {

        // Mock logic: always allow
        return true;
    }

    private String generateRma() {
        return "RMA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

