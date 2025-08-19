package com.iitp.domains.order.domain.entity;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Order extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    // TODO: 장바구니 엔티티 구현시 연관관계 설정
    // TODO: 쿠폰리스트 엔티티 구현시 연관관계 설정 (쿠폰은 후순위)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // 픽업 시간
    @Column(name = "pickup_due_time", nullable = false)
    private LocalDateTime pickupDueTime;

    // 결제전 총 금액
    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;


    public boolean isCompleted() {  // 디미터 법칙 고민
        return status.equals(OrderStatus.COMPLETED);
    }

}
