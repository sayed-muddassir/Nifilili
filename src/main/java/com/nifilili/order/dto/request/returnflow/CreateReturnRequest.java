package com.nifilili.order.dto.request.returnflow;

import com.nifilili.core.dto.AddressDto;
import lombok.Data;

import java.util.List;

@Data
public class CreateReturnRequest {
    private String reason;
    private String details;
    private List<String> photos;
    private AddressDto pickupAddress;
}

