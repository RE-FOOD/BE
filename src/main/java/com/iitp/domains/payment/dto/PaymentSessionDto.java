package com.iitp.domains.payment.dto;

import lombok.Builder;

import java.io.Serializable;
import java.sql.Timestamp;

@Builder
public record PaymentSessionDto(
        String sessionId,         // 결제 세션 ID
        String orderId,           // 임시 주문 ID
        String status,            // 결제 상태 (PENDING, SUCCESS, FAILED)
        Timestamp createdAt,   // 생성 시간
        Timestamp expiresAt   // 만료 시간
)implements Serializable {
}
