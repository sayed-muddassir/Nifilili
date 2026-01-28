package com.nifilili.order.dto.response.returnflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private Long returnRequestId;
    private String rmaNumber;
    private String status;
}

