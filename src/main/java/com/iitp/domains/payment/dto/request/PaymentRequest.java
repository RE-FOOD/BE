package com.iitp.domains.payment.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String orderId;           // 주문 ID
    private String orderName;         // 주문명
    private Long amount;              // 결제 금액
    private String customerName;      // 고객명
    private String customerEmail;     // 고객 이메일
    private String customerPhone;     // 고객 전화번호
    private String successUrl;        // 성공 시 리다이렉트 URL
    private String failUrl;           // 실패 시 리다이렉트 URL
}