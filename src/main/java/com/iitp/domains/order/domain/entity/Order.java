package com.iitp.domains.order.domain.entity;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
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
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // 픽업 시간
    @Column(name = "pickup_due_time", nullable = false)
    private Timestamp pickupDueTime;

    // 결제전 총 금액
    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    // 다회 용기 사용 여부
    @Column(name = "is_container_reused", nullable = false)
    private Boolean isContainerReused = false;  // 다회용기 사용 여부

    public boolean isCompleted() {  // 디미터 법칙 고민
        return status.equals(OrderStatus.COMPLETED);
    }

}
