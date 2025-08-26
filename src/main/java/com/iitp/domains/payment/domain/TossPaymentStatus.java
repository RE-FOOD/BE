package com.iitp.domains.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossPaymentStatus {
    ABORTED("중단"),
    CANCELED("취소"),
    DONE("완료"),
    EXPIRED("만료"),
    IN_PROGRESS("진행중"),
    PARTIAL_CANCELED("부분 취소"),
    READY("준비"),
    WAITING_FOR_DEPOSIT("입금 대기중");

    private final String description;
}

