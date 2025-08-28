package com.iitp.domains.order.dto.response;

import com.iitp.domains.payment.domain.TossPaymentMethod;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

@Builder
public record OrderPaymentResponse(
        String storeName,
        String orderNumber,
        Timestamp requestedAt,
        List<OrderPaymentMenuList> menus,
        int totalAmount,
        TossPaymentMethod paymentMethod,
        String memberName,
        String memberNumber,
        Timestamp pickupDueTime
) {
}
