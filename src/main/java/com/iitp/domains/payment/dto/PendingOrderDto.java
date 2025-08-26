package com.iitp.domains.payment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;



@Builder
public record PendingOrderDto(
        String sessionId,         // 결제 세션 ID
        Long memberId,            // 회원 ID
        Long storeId,             // 가게 ID
        String storeName,
        Long cartId,              // 장바구니 ID
        int totalAmount,         // 총 금액
        Timestamp pickupDueTime,  // 픽업 시간
        boolean isContainerReused, // 용기 재사용 여부
        Timestamp createdAt   // 생성 시간
) implements Serializable{
}