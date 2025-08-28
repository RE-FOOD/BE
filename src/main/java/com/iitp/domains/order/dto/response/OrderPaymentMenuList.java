package com.iitp.domains.order.dto.response;

import lombok.Builder;

@Builder
public record OrderPaymentMenuList(
        String name,
        int quality,
        int totalAmount
) {
}
