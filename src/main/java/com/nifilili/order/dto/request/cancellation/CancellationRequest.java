package com.nifilili.order.dto.request.cancellation;

import lombok.Data;

import java.util.List;

@Data
public class CancellationRequest {
    private List<CancellationItemRequest> items;
}

