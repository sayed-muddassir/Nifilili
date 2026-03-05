package com.nifilili.order.service.impl;

import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.ReturnStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.core.security.SecurityUtil;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.OrderStatusHistoryEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.request.CreateReturnRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.mapper.ReturnMapper;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.repository.ReturnRequestRepository;
import com.nifilili.order.service.BusinessConfigService;
import com.nifilili.order.service.ReturnService;
import com.nifilili.order.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReturnServiceImpl implements ReturnService {

    private static final String CONFIG_RETURN_WINDOW_DAYS = "return_window_days";
    private static final int DEFAULT_RETURN_WINDOW_DAYS = 7;

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final BusinessConfigService businessConfigService;
    private final ReturnMapper returnMapper;

    @Override
    public ReturnResponse createReturn(Long orderItemId, CreateReturnRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Creating return request for orderItemId={}", orderItemId);
        LocalDateTime now = LocalDateTime.now();

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        if (item.getStatus() != OrderItemStatus.DELIVERED) {
            throw new InvalidOrderStateException("Can only return delivered items");
        }

        // Check return window
        Map<Long, Map<String, String>> configs = businessConfigService
                .getConfigMap(Set.of(item.getBusinessId()));
        Map<String, String> businessConfig = configs.getOrDefault(item.getBusinessId(), Map.of());
        int returnWindowDays = Integer.parseInt(
                businessConfig.getOrDefault(CONFIG_RETURN_WINDOW_DAYS,
                        String.valueOf(DEFAULT_RETURN_WINDOW_DAYS)));

        long daysSinceDelivery = ChronoUnit.DAYS.between(item.getUpdatedAt().toLocalDate(), now.toLocalDate());
        if (daysSinceDelivery > returnWindowDays) {
            throw new InvalidOrderStateException(
                    "Return window of " + returnWindowDays + " days has expired");
        }

        // Check for existing return
        if (returnRequestRepository.findByOrderItemId(orderItemId).isPresent()) {
            throw new InvalidOrderStateException("Return request already exists for this item");
        }

        ReturnRequestEntity returnRequest = ReturnRequestEntity.builder()
                .orderItemId(orderItemId)
                .reason(request.getReason())
                .reasonDetails(request.getDetails())
                .photos(request.getPhotos())
                .pickupAddress(request.getPickupAddress() != null
                        ? Map.of(
                        "municipalityId", request.getPickupAddress().getMunicipalityId(),
                        "wardNumber", request.getPickupAddress().getWardNumber(),
                        "toleName", request.getPickupAddress().getToleName(),
                        "addressField1", request.getPickupAddress().getAddressField1())
                        : Map.of())
                .status(ReturnStatus.REQUESTED)
                .rmaNumber(OrderNumberGenerator.generateRmaNumber())
                .requestedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        returnRequest = returnRequestRepository.save(returnRequest);

        // Record status history
        statusHistoryRepository.save(OrderStatusHistoryEntity.builder()
                .orderItemId(orderItemId)
                .oldStatus(OrderItemStatus.DELIVERED.name())
                .newStatus("RETURN_REQUESTED")
                .createdBy(userId)
                .createdAt(now)
                .build());

        return returnMapper.toResponse(returnRequest);
    }
}
