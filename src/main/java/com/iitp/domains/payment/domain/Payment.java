package com.iitp.domains.payment.domain;

import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Payment extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "payment_key",nullable = false, unique = true)
    private String paymentKey;      //  Toss Payments에서 제공하는 결제에 대한 식별 값

    // 토스내부에서 관리하는 별도의 orderId가 존재함
    @Column(name = "toss_order_id",nullable = false)
    private String tossOrderId;      // 프론트에서 지정한 orderId.

    @Enumerated(value = EnumType.STRING)
    @Column(name = "toss_payment_method",nullable = false)
    private TossPaymentMethod tossPaymentMethod;      // 결제 방식

    @Enumerated(value = EnumType.STRING)
    @Column(name = "toss_payment_status",nullable = false)
    private TossPaymentStatus tossPaymentStatus;      // 결제 상태

    private long totalAmount;      // 총 결제 금액

    @Column(name = "requested_at",nullable = false)
    private LocalDateTime requestedAt;      // 결제 요청 시간

    @Column(name = "approved_at",nullable = false)
    private LocalDateTime approvedAt;      // 결제 승인 시간
}
