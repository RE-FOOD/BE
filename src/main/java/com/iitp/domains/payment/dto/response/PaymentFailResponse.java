package com.iitp.domains.payment.dto.response;

public record PaymentFailResponse(
    String code,
    String message
) {
}