package com.iitp.domains.payment.dto.request;

import lombok.Getter;
import lombok.Setter;


public record PaymentRequest(
        String paymentKey,
        String orderId,
        String amount
){
}
