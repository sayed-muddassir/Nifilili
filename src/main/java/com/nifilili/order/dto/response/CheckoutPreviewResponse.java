package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutPreviewResponse {
    private List<CheckoutBusinessGroupResponse> businessGroups;
    private CheckoutSummaryResponse summary;
}
