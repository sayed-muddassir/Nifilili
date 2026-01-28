package com.nifilili.order.factory;

import com.nifilili.order.dto.response.checkout.*;
import com.nifilili.order.model.PricingItem;
import com.nifilili.order.model.PricingResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CheckoutFactory converts PricingResult (domain output)
 * into CheckoutPreviewResponse (API response).
 *
 * This mapper MUST remain:
 *  - stateless
 *  - side-effect free
 *  - logic-free
 */
public final class CheckoutFactory {

    private CheckoutFactory() {
        // Utility class – prevent instantiation
    }

    /**
     * Convert pricing result to checkout preview response.
     */
    public static CheckoutPreviewResponse toPreviewResponse(
            PricingResult pricingResult
    ) {

        CheckoutPreviewResponse response = new CheckoutPreviewResponse();

        // 1️⃣ Map item-level pricing
        response.setItems(
                mapItems(pricingResult.getItems())
        );

        // 2️⃣ Map aggregated summary
        response.setSummary(
                mapSummary(pricingResult)
        );

        return response;
    }

    // ------------------------------------------------------------------
    // Internal mapping helpers
    // ------------------------------------------------------------------

    /**
     * Map pricing items → checkout preview items.
     */
    private static List<CheckoutItemPreviewResponse> mapItems(
            List<PricingItem> pricingItems
    ) {

        return pricingItems.stream()
                .map(item -> {

                    CheckoutItemPreviewResponse response =
                            new CheckoutItemPreviewResponse();

                    response.setBusinessId(item.getBusinessId());
                    response.setOfferingId(item.getOfferingId());
                    response.setTitle(item.getTitle()); // snapshot or placeholder
                    response.setQuantity(item.getQuantity());
                    response.setUnitPrice(item.getUnitPrice());

                    response.setDiscount(item.getDiscount());
                    response.setTax(item.getTax());
                    response.setDeliveryCharge(item.getDeliveryCharge());
                    response.setSubtotal(item.getSubTotal());

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map pricing summary → checkout summary.
     */
    private static CheckoutSummaryResponse mapSummary(
            PricingResult pricingResult
    ) {

        CheckoutSummaryResponse summary = new CheckoutSummaryResponse();

        summary.setSubtotal(
                pricingResult.getSummary().getSubTotal()
        );
        summary.setTotalDiscount(
                pricingResult.getSummary().getTotalDiscount()
        );
        summary.setTotalTax(
                pricingResult.getSummary().getTotalTax()
        );
        summary.setDeliveryCharge(
                pricingResult.getSummary().getDeliveryCharge()
        );
        summary.setPayableAmount(
                pricingResult.getSummary().getPayableAmount()
        );

        return summary;
    }
}

