package com.nifilili.order.dto.request.orderplacement;

import com.nifilili.core.dto.AddressDto;
import com.nifilili.order.dto.request.checkout.CouponApplyRequest;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PlaceOrderRequest {
    private String receiverName;
    private String contactNumber;
    private String email;
    private AddressDto address;
    private String customerNotes;
    private Long paymentTypeId;
    private Map<String, Object> paymentDetails;
    private List<CouponApplyRequest> coupons;
}

