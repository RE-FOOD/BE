package com.iitp.domains.notification.dto;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.notification.domain.entity.NotificationType;
import com.iitp.domains.order.domain.entity.Order;
import lombok.Builder;

public record NotifyParams(
        Member receiver, NotificationType type, String title, String content
) {
    @Builder
    public NotifyParams {
    }

    // 레벨업
    public static NotifyParams ofEnvironmentLevelUp(Member member) {

        String content = """
                환경 레벨 %s 달성!
                축하합니다!
                """.formatted(member.getEnvironmentLevel().getLevel());
        return NotifyParams.builder()
                .receiver(member)
                .type(NotificationType.ENVIRONMENT_LEVEL_UP)
                .title(NotificationType.ENVIRONMENT_LEVEL_UP.getTitle())
                .content(content)
                .build();
    }

    // 주문 완료
    public static NotifyParams ofOrderCompletion(Order order) {
        String content = """
                %s 가게의 주문이 접수되었습니다!
                """.formatted(order.getStore().getName());
        return NotifyParams.builder()
                .receiver(order.getMember())
                .type(NotificationType.ORDER_COMPLETION)
                .title(NotificationType.ORDER_COMPLETION.getTitle())
                .content(content)
                .build();
    }

    // 주문 취소
    public static NotifyParams ofOrderRefusal(Order order) {
        String content = """
                %s 가게 사정으로 인하여 주문이 취소되었습니다. 결제 금액은 전액 환불처리됩니다.
                """.formatted(order.getStore().getName());
        return NotifyParams.builder()
                .receiver(order.getMember())
                .type(NotificationType.ORDER_CANCELED)
                .title(NotificationType.ORDER_CANCELED.getTitle())
                .content(content)
                .build();
    }

    // 픽업 안내
    public static NotifyParams ofOrderPickUp(Member member, Order order) {
        String content = """
                %s 가게 픽업 시간 5분 전입니다. ~알림 내용~
                """.formatted(order.getStore().getName());
        return NotifyParams.builder()
                .receiver(member)
                .type(NotificationType.ORDER_PICK_UP)
                .title(NotificationType.ORDER_PICK_UP.getTitle())
                .content(content)
                .build();
    }

}
