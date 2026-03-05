package com.nifilili.order.controller.publicapi;

import com.nifilili.core.constants.SwaggerConstants;
import com.nifilili.order.dto.request.CheckoutPreviewRequest;
import com.nifilili.order.dto.response.CheckoutPreviewResponse;
import com.nifilili.order.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = SwaggerConstants.ORDER_2, description = "Checkout preview and pricing calculation")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preview")
    @Operation(summary = "Step 2.1: Checkout Preview", description = "Generates a checkout preview with pricing, taxes, and discounts")
    @ApiResponse(responseCode = "200", description = "Preview generated")
    @ApiResponse(responseCode = "400", description = "Cart is empty or invalid")
    public ResponseEntity<CheckoutPreviewResponse> preview(@Valid @RequestBody CheckoutPreviewRequest request) {
        log.info("POST /api/v1/checkout/preview");
        return ResponseEntity.ok(checkoutService.preview(request));
    }
}
