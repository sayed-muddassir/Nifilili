package com.nifilili.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderTimelineResponse {
    private String status;
    private LocalDateTime at;
    private String note;
}
