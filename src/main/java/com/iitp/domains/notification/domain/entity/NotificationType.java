package com.iitp.domains.notification.domain.entity;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.order.domain.entity.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationType {

    /**
     * 레벨업
     * 레벨 N 달성
     * 주문 완료 및 환경 레벨업 요구 포인트 달성시 알림
     */
    ENVIRONMENT_LEVEL_UP(ReceiverType.USER, Member.class, "레벨업"),

    /**
     * 주문 완료, 취소
     * 사장님의 주문 처리에 따른 알림
     */
    ORDER_COMPLETION(ReceiverType.USER, Order.class, "주문 완료"),
    ORDER_CANCELED(ReceiverType.USER, Order.class, "주문 취소"),

    /**
     * 픽업 안내
     * 5분전 스케줄링을 통한 알림
     */
    ORDER_PICK_UP(ReceiverType.USER, Order.class, "픽업 안내"),

    ;

    private enum ReceiverType {
        USER, STORE_OWNER, ADMIN
    }

    private final ReceiverType receiverType;
    private final Class<?> redirectTargetClass;
    private final String title;
}
