package com.nifilili.order.dto.response.orderplacement;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTimelineResponse {
    private String status;
    private LocalDateTime at;
    private String note;
}

