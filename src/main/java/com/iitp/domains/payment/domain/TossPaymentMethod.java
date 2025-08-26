package com.iitp.domains.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossPaymentMethod {
    EASYPAYMENT("간편결제"),
    ACCOUNT("계좌이체"),
    CARD("카드");
    private final String description;
}