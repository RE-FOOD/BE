package com.iitp.domains.payment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse {
    private String paymentKey;        // 결제 키
    private String orderId;           // 주문 ID
    private String orderName;         // 주문명
    private Long amount;              // 결제 금액
    private String status;            // 결제 상태
    private String method;            // 결제 수단
    private String requestedAt;       // 요청 시간
    private String approvedAt;        // 승인 시간
}

