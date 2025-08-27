package com.iitp.domains.payment.dto.response;

import lombok.Getter;
import lombok.Setter;


public record PaymentResponse(
        String sessionId,
        String storeName,
        Integer totalAmount
) {
}
