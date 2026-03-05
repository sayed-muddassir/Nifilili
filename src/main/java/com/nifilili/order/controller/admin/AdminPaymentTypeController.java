package com.nifilili.order.controller.admin;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.response.PaymentTypeResponse;
import com.nifilili.order.repository.PaymentTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/payment-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = SwaggerConstants.ORDER_12, description = "Admin management of payment types")
public class AdminPaymentTypeController {

    private final PaymentTypeRepository paymentTypeRepository;

    @GetMapping
    @Operation(summary = "List payment types", description = "Returns all active payment types")
    @ApiResponse(responseCode = "200", description = "Payment types returned")
    public ResponseEntity<List<PaymentTypeResponse>> listPaymentTypes() {
        log.info("GET /api/v1/admin/payment-types");
        List<PaymentTypeResponse> responses = paymentTypeRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{paymentTypeId}")
    @Operation(summary = "Deactivate payment type", description = "Deactivates a payment type (soft delete)")
    @ApiResponse(responseCode = "204", description = "Payment type deactivated")
    @ApiResponse(responseCode = "404", description = "Payment type not found")
    public ResponseEntity<Void> deactivate(@PathVariable Long paymentTypeId) {
        log.info("DELETE /api/v1/admin/payment-types/{}", paymentTypeId);
        PaymentTypeEntity entity = paymentTypeRepository.findById(paymentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment type not found"));
        entity.setIsActive(false);
        entity.setUpdatedAt(LocalDateTime.now());
        paymentTypeRepository.save(entity);
        return ResponseEntity.noContent().build();
    }

    private PaymentTypeResponse toResponse(PaymentTypeEntity entity) {
        return PaymentTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }
}
